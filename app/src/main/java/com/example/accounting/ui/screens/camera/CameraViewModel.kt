package com.example.accounting.ui.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.ai.AiConfig
import com.example.accounting.ai.AiSettingsManager
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.InputSource
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.CategoryRepository
import com.example.accounting.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class CameraUiState(
    val isProcessing: Boolean = false,
    val recognizedText: String = "",
    val parsedAmount: String = "",
    val parsedMerchant: String = "",
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val imagePath: String? = null,
    val showResult: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val aiSettingsManager: AiSettingsManager
) : ViewModel() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getByType(TransactionType.EXPENSE).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun processImage(context: Context, uri: Uri) {
        _uiState.update { it.copy(isProcessing = true, showResult = false, isSaved = false, imagePath = uri.toString()) }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    // 读取图片并转为Base64
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val base64Image = bitmapToBase64(bitmap)

                    // 调用AI API解析图片
                    parseImageWithAi(base64Image)
                }

                // 更新解析结果
                _uiState.update {
                    it.copy(
                        recognizedText = result.note,
                        parsedAmount = result.amount.toString(),
                        parsedMerchant = result.note,
                        showResult = true
                    )
                }

                // 选中匹配的分类
                val categories = _uiState.value.categories
                val matchedCategory = categories.find { it.name == result.category }
                    ?: categories.find { it.name == "其他" }

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        selectedCategory = matchedCategory
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "识别失败: ${e.message}"
                    )
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private suspend fun parseImageWithAi(base64Image: String): AiParseResult {
        val apiKey = aiSettingsManager.getEffectiveApiKey()
        val apiUrl = aiSettingsManager.getEffectiveApiUrl()
        val model = aiSettingsManager.getEffectiveModelName()

        val prompt = """你是一个智能记账助手。请分析这张图片（票据/小票/账单），提取记账信息。

请用以下格式回复（每行一个字段）：
金额：xxx元
类型：支出/收入
分类：xxx
备注：xxx

分类规则：
- 支出分类：餐饮、交通、购物、娱乐、居住、医疗、教育、通讯、服饰、其他
- 收入分类：工资、奖金、投资、兼职、其他

如果看不清或无法识别，请回复：金额：0元"""

        val requestBody = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            }
                        }
                    }
                }
            }
            put("max_tokens", 500)
            put("temperature", 0.3)
        }.toString()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        val content = json.parseToJsonElement(body).jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: throw Exception("Invalid response format")

        return parseAiResponse(content)
    }

    private fun parseAiResponse(response: String): AiParseResult {
        val lines = response.lines().map { it.trim() }

        var amount = 0.0
        var category = "其他"
        var note = ""
        var type = "expense"

        for (line in lines) {
            when {
                line.startsWith("金额") -> {
                    val amountStr = line.replace(Regex("[^0-9.]"), "")
                    amount = amountStr.toDoubleOrNull() ?: 0.0
                }
                line.startsWith("类型") -> {
                    val typeStr = line.removePrefix("类型").removePrefix("：").removePrefix(":").trim()
                    if (typeStr.contains("收入")) {
                        type = "income"
                    }
                }
                line.startsWith("分类") -> {
                    category = line.removePrefix("分类").removePrefix("：").removePrefix(":").trim()
                }
                line.startsWith("备注") -> {
                    note = line.removePrefix("备注").removePrefix("：").removePrefix(":").trim()
                }
            }
        }

        if (note.isBlank()) {
            note = response.take(50)
        }

        return AiParseResult(amount, category, note, type)
    }

    private data class AiParseResult(
        val amount: Double,
        val category: String,
        val note: String,
        val type: String
    )

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(parsedAmount = amount, errorMessage = null) }
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(parsedMerchant = merchant) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.parsedAmount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效金额") }
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                categoryId = state.selectedCategory?.id,
                categoryName = state.selectedCategory?.name ?: "其他",
                note = state.parsedMerchant,
                date = System.currentTimeMillis(),
                source = InputSource.OCR,
                imagePath = state.imagePath
            )
            transactionRepository.insert(transaction)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
