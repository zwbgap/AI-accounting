package com.example.accounting.ai

/**
 * AI服务配置 - 使用智谱API
 */
object AiConfig {
    // 智谱API Key - 在 https://open.bigmodel.cn/ 获取
    const val API_KEY = "2c3b163e970040609ea5d2b7dc6e7fdf"

    // 智谱API端点
    const val API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"

    // 模型名称（支持图片识别的模型）
    const val MODEL = "glm-4v"
}
