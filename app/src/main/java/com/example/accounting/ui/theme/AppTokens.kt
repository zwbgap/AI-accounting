package com.example.accounting.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 设计 Token 系统 - 借鉴 BeeCount 的 BeeTokens
 * 统一管理颜色、间距、文字样式，支持明暗主题自动切换
 */
object AppTokens {

    // ==================== 颜色 Token ====================

    // 语义色 - 收入/支出
    @Composable fun incomeColor() = if (isSystemInDarkTheme()) Color(0xFF66BB6A) else Color(0xFF2E7D32)
    @Composable fun expenseColor() = if (isSystemInDarkTheme()) Color(0xFFEF5350) else Color(0xFFD32F2F)
    @Composable fun transferColor() = if (isSystemInDarkTheme()) Color(0xFF42A5F5) else Color(0xFF1976D2)

    // 表面色
    @Composable fun surfacePrimary() = MaterialTheme.colorScheme.surface
    @Composable fun surfaceSecondary() = MaterialTheme.colorScheme.surfaceVariant
    @Composable fun surfaceCard() = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    @Composable fun surfaceElevated() = if (isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFFF5F5F5)

    // 文字色
    @Composable fun textPrimary() = MaterialTheme.colorScheme.onSurface
    @Composable fun textSecondary() = MaterialTheme.colorScheme.onSurfaceVariant
    @Composable fun textTertiary() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    @Composable fun textOnPrimary() = MaterialTheme.colorScheme.onPrimary

    // 边框色
    @Composable fun borderLight() = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    @Composable fun borderMedium() = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    // 语义状态色
    @Composable fun successColor() = Color(0xFF4CAF50)
    @Composable fun warningColor() = Color(0xFFFF9800)
    @Composable fun errorColor() = MaterialTheme.colorScheme.error
    @Composable fun infoColor() = Color(0xFF2196F3)

    // 图表色板 - 12色
    val chartColors = listOf(
        Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0),
        Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF9CCC65),
        Color(0xFFFFEE58), Color(0xFFFFA726), Color(0xFF8D6E63),
        Color(0xFF78909C), Color(0xFFEC407A), Color(0xFF7E57C2)
    )

    // ==================== 间距 Token ====================

    object Spacing {
        val xs = 4.sp
        val sm = 8.sp
        val md = 12.sp
        val lg = 16.sp
        val xl = 20.sp
        val xxl = 24.sp
    }

    object Radius {
        val sm = 8
        val md = 12
        val lg = 16
        val xl = 20
        val full = 999
    }

    // ==================== 文字样式 Token ====================

    @Composable fun titleStyle() = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textPrimary()
    )

    @Composable fun subtitleStyle() = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = textSecondary()
    )

    @Composable fun bodyStyle() = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = textPrimary()
    )

    @Composable fun labelStyle() = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = textSecondary()
    )

    @Composable fun amountStyle() = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = textPrimary()
    )

    @Composable fun largeAmountStyle() = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = textOnPrimary()
    )
}
