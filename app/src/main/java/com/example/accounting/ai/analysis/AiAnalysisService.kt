package com.example.accounting.ai.analysis

import com.example.accounting.ai.AiConfig
import com.example.accounting.ai.AiSettingsManager
import com.example.accounting.data.db.dao.CategoryTotal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * AI消费分析服务 - 使用智谱API生成消费建议
 */
class AiAnalysisService(
    private val settingsManager: AiSettingsManager? = null
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 根据消费数据生成AI建议（完全依赖AI）
     */
    suspend fun generateAdvice(
        totalIncome: Double,
        totalExpense: Double,
        categoryTotals: List<CategoryTotal>,
        period: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val advice = generateWithApi(totalIncome, totalExpense, categoryTotals, period)
            Result.success(advice)
        } catch (e: Exception) {
            Result.failure(Exception("AI分析失败: ${e.message}"))
        }
    }

    private suspend fun generateWithApi(
        totalIncome: Double,
        totalExpense: Double,
        categoryTotals: List<CategoryTotal>,
        period: String
    ): String {
        val apiKey = settingsManager?.getEffectiveApiKey() ?: AiConfig.API_KEY
        val apiUrl = settingsManager?.getEffectiveApiUrl() ?: AiConfig.API_URL
        val model = settingsManager?.getEffectiveModelName() ?: AiConfig.MODEL

        val categoryInfo = categoryTotals.joinToString("\n") { "  - ${it.categoryName}: ¥${it.total}" }

        val prompt = """分析以下${period}消费数据，给出简短的消费建议（100字以内）：

收入：¥$totalIncome
支出：¥$totalExpense
结余：¥${totalIncome - totalExpense}

支出明细：
$categoryInfo

请从以下角度给出建议：
1. 消费结构是否合理
2. 哪些方面可以优化
3. 储蓄建议"""

        val requestBody = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
            put("temperature", 0.7)
        }.toString()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        return json.parseToJsonElement(body).jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: "暂无建议"
    }

}
