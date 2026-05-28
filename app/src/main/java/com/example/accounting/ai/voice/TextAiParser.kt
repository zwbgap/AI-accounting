package com.example.accounting.ai.voice

import com.example.accounting.ai.AiConfig
import com.example.accounting.ai.AiSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class ParsedTransaction(
    val amount: Double,
    val category: String,
    val note: String,
    val date: String? = null,
    val type: String = "expense"  // "expense" or "income"
)

/**
 * 使用智谱API解析自然语言记账
 */
class TextAiParser(
    private val settingsManager: AiSettingsManager? = null
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 解析自然语言为结构化账单（完全依赖AI）
     */
    suspend fun parse(input: String): Result<ParsedTransaction> = withContext(Dispatchers.IO) {
        try {
            val result = parseWithApi(input)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("AI解析失败: ${e.message}"))
        }
    }

    private suspend fun parseWithApi(input: String): ParsedTransaction {
        val apiKey = settingsManager?.getEffectiveApiKey() ?: AiConfig.API_KEY
        val apiUrl = settingsManager?.getEffectiveApiUrl() ?: AiConfig.API_URL
        val model = settingsManager?.getEffectiveModelName() ?: AiConfig.MODEL

        val prompt = """你是一个智能记账助手。用户会告诉你他们的消费或收入情况，你需要理解并提取记账信息。

请用以下格式回复（每行一个字段）：
金额：xxx元
类型：支出/收入
分类：xxx
备注：xxx
日期：xxx（如果用户没说就写"今天"）

分类规则：
- 支出分类：餐饮、交通、购物、娱乐、居住、医疗、教育、通讯、服饰、其他
- 收入分类：工资、奖金、投资、兼职、其他

用户说：$input"""

        val requestBody = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", "你是一个友好的记账助手，帮助用户记录消费和收入。请简洁回复，只输出记账信息。")
                }
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
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

        val jsonResponse = json.parseToJsonElement(body).jsonObject
        val content = jsonResponse["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: throw Exception("Invalid response format")

        return parseAiResponse(content)
    }

    /**
     * 解析AI的自然语言回复
     */
    private fun parseAiResponse(response: String): ParsedTransaction {
        val lines = response.lines().map { it.trim() }

        var amount = 0.0
        var category = "其他"
        var note = ""
        var date: String? = null
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
                line.startsWith("日期") -> {
                    val dateStr = line.removePrefix("日期").removePrefix("：").removePrefix(":").trim()
                    if (dateStr != "今天" && dateStr.isNotBlank()) {
                        date = dateStr
                    }
                }
            }
        }

        // 如果备注为空，使用用户输入作为备注
        if (note.isBlank()) {
            note = response.take(50)
        }

        return ParsedTransaction(
            amount = amount,
            category = category,
            note = note,
            date = date,
            type = type
        )
    }

}
