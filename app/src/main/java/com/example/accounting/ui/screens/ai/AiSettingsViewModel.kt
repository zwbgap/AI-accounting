package com.example.accounting.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.ai.AiConfig
import com.example.accounting.ai.AiSettingsManager
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// 供应商模型配置
data class AiProvider(
    val name: String,
    val apiUrl: String,
    val models: List<String>
)

val aiProviders = listOf(
    AiProvider(
        name = "智谱AI",
        apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
        models = listOf("glm-4-flash", "glm-4", "glm-4v")
    ),
    AiProvider(
        name = "OpenAI",
        
        piUrl = "https://api.openai.com/v1/chat/completions",
        models = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo")
    ),
    AiProvider(
        name = "通义千问",
        apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
        models = listOf("qwen-turbo", "qwen-plus", "qwen-max", "qwen-long")
    ),
    AiProvider(
        name = "文心一言",
        apiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions",
        models = listOf("ernie-4.0-8k", "ernie-3.5-8k", "ernie-speed-128k")
    ),
    AiProvider(
        name = "DeepSeek",
        apiUrl = "https://api.deepseek.com/chat/completions",
        models = listOf("deepseek-chat", "deepseek-coder")
    ),
    AiProvider(
        name = "自定义",
        apiUrl = "",
        models = emptyList()
    )
)

data class AiSettingsUiState(
    val isAiEnabled: Boolean = true,
    val apiKey: String = "",
    val apiUrl: String = "",
    val model: String = AiConfig.MODEL,
    val selectedProvider: String = "智谱AI",
    val isOcrEnabled: Boolean = true,
    val isVoiceEnabled: Boolean = true,
    val isAiCategoryEnabled: Boolean = true,
    val isAiParseEnabled: Boolean = true,
    val isAiAdviceEnabled: Boolean = true,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val testSuccess: Boolean? = null
)

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val aiSettingsManager: AiSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val currentApiUrl = aiSettingsManager.apiUrl
        val currentModel = aiSettingsManager.modelName

        // 根据API URL判断供应商
        val provider = aiProviders.find { provider ->
            provider.apiUrl.isNotBlank() && currentApiUrl.contains(provider.apiUrl.substringAfter("https://").substringBefore("/"))
        } ?: aiProviders.last() // 默认自定义

        _uiState.update {
            AiSettingsUiState(
                isAiEnabled = aiSettingsManager.isAiEnabled,
                apiKey = aiSettingsManager.apiKey,
                apiUrl = currentApiUrl,
                model = currentModel,
                selectedProvider = provider.name,
                isOcrEnabled = aiSettingsManager.isOcrEnabled,
                isVoiceEnabled = aiSettingsManager.isVoiceEnabled,
                isAiCategoryEnabled = aiSettingsManager.isAiCategoryEnabled,
                isAiParseEnabled = aiSettingsManager.isAiParseEnabled,
                isAiAdviceEnabled = aiSettingsManager.isAiAdviceEnabled
            )
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        aiSettingsManager.isAiEnabled = enabled
        _uiState.update { it.copy(isAiEnabled = enabled) }
    }

    fun updateApiKey(key: String) {
        aiSettingsManager.apiKey = key
        _uiState.update { it.copy(apiKey = key) }
    }

    fun updateApiUrl(url: String) {
        aiSettingsManager.apiUrl = url
        _uiState.update { it.copy(apiUrl = url) }
    }

    fun updateModelName(model: String) {
        aiSettingsManager.modelName = model
        _uiState.update { it.copy(model = model) }
    }

    fun selectProvider(providerName: String) {
        _uiState.update {
            it.copy(selectedProvider = providerName)
        }
    }

    fun testConnection() {
        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(testResult = "请先填写API Key", testSuccess = false) }
            return
        }

        _uiState.update { it.copy(isTesting = true, testResult = null, testSuccess = null) }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val requestBody = buildJsonObject {
                        put("model", state.model)
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", "你好")
                            }
                        }
                        put("max_tokens", 10)
                    }.toString()

                    val request = Request.Builder()
                        .url(state.apiUrl)
                        .addHeader("Authorization", "Bearer ${state.apiKey}")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    val code = response.code
                    val body = response.body?.string() ?: ""

                    if (code == 200) {
                        "连接成功！模型: ${state.model}"
                    } else {
                        "连接失败 (HTTP $code): ${body.take(100)}"
                    }
                }
                _uiState.update { it.copy(isTesting = false, testResult = result, testSuccess = result.startsWith("连接成功")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isTesting = false, testResult = "连接失败: ${e.message}", testSuccess = false) }
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null, testSuccess = null) }
    }

    fun setOcrEnabled(enabled: Boolean) {
        aiSettingsManager.isOcrEnabled = enabled
        _uiState.update { it.copy(isOcrEnabled = enabled) }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        aiSettingsManager.isVoiceEnabled = enabled
        _uiState.update { it.copy(isVoiceEnabled = enabled) }
    }

    fun setAiCategoryEnabled(enabled: Boolean) {
        aiSettingsManager.isAiCategoryEnabled = enabled
        _uiState.update { it.copy(isAiCategoryEnabled = enabled) }
    }

    fun setAiParseEnabled(enabled: Boolean) {
        aiSettingsManager.isAiParseEnabled = enabled
        _uiState.update { it.copy(isAiParseEnabled = enabled) }
    }

    fun setAiAdviceEnabled(enabled: Boolean) {
        aiSettingsManager.isAiAdviceEnabled = enabled
        _uiState.update { it.copy(isAiAdviceEnabled = enabled) }
    }
}
