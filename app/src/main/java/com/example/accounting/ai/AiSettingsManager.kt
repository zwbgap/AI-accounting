package com.example.accounting.ai

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI设置管理器 - 管理AI功能开关和API配置
 */
@Singleton
class AiSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    // AI功能开关
    var isAiEnabled: Boolean
        get() = prefs.getBoolean("ai_enabled", true)
        set(value) = prefs.edit().putBoolean("ai_enabled", value).apply()

    // API Key
    var apiKey: String
        get() = prefs.getString("api_key", AiConfig.API_KEY) ?: AiConfig.API_KEY
        set(value) = prefs.edit().putString("api_key", value).apply()

    // API URL
    var apiUrl: String
        get() = prefs.getString("api_url", AiConfig.API_URL) ?: AiConfig.API_URL
        set(value) = prefs.edit().putString("api_url", value).apply()

    // 模型名称
    var modelName: String
        get() = prefs.getString("model_name", AiConfig.MODEL) ?: AiConfig.MODEL
        set(value) = prefs.edit().putString("model_name", value).apply()

    // 兼容旧代码
    var zhipuApiKey: String
        get() = apiKey
        set(value) { apiKey = value }

    // OCR功能开关
    var isOcrEnabled: Boolean
        get() = prefs.getBoolean("ocr_enabled", true)
        set(value) = prefs.edit().putBoolean("ocr_enabled", value).apply()

    // 语音识别功能开关
    var isVoiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) = prefs.edit().putBoolean("voice_enabled", value).apply()

    // AI分类功能开关
    var isAiCategoryEnabled: Boolean
        get() = prefs.getBoolean("ai_category_enabled", true)
        set(value) = prefs.edit().putBoolean("ai_category_enabled", value).apply()

    // AI文本解析功能开关
    var isAiParseEnabled: Boolean
        get() = prefs.getBoolean("ai_parse_enabled", true)
        set(value) = prefs.edit().putBoolean("ai_parse_enabled", value).apply()

    // AI消费建议功能开关
    var isAiAdviceEnabled: Boolean
        get() = prefs.getBoolean("ai_advice_enabled", true)
        set(value) = prefs.edit().putBoolean("ai_advice_enabled", value).apply()

    /**
     * 获取当前有效的API Key
     */
    fun getEffectiveApiKey(): String {
        return if (apiKey.isNotBlank()) apiKey else AiConfig.API_KEY
    }

    /**
     * 获取当前有效的API URL
     */
    fun getEffectiveApiUrl(): String {
        return if (apiUrl.isNotBlank()) apiUrl else AiConfig.API_URL
    }

    /**
     * 获取当前有效的模型名称
     */
    fun getEffectiveModelName(): String {
        return if (modelName.isNotBlank()) modelName else AiConfig.MODEL
    }

    /**
     * 检查AI功能是否可用
     */
    fun isAiAvailable(): Boolean {
        return isAiEnabled && getEffectiveApiKey().isNotBlank()
    }
}
