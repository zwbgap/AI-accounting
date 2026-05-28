package com.example.accounting.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.accounting.ui.theme.ExpenseColor
import com.example.accounting.ui.theme.IncomeColor
import com.example.accounting.util.DateUtils

// 饼图颜色列表
val pieColors = listOf(
    Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0),
    Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF9CCC65),
    Color(0xFFFFEE58), Color(0xFFFFA726), Color(0xFF8D6E63),
    Color(0xFF78909C)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 时间选择器
        item {
            PeriodSelector(
                period = uiState.period,
                year = uiState.year,
                month = uiState.month,
                onPeriodSelected = viewModel::updatePeriod,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )
        }

        // 收支总览
        item {
            SummaryCard(
                income = uiState.totalIncome,
                expense = uiState.totalExpense
            )
        }

        // 分类统计
        item {
            Text(
                "支出分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.categoryTotals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(uiState.categoryTotals.indices.toList()) { index ->
            val total = uiState.categoryTotals[index]
            val percentage = if (uiState.totalExpense > 0) (total.total / uiState.totalExpense * 100) else 0.0
            CategoryStatItem(
                name = total.categoryName,
                amount = total.total,
                percentage = percentage,
                color = pieColors[index % pieColors.size]
            )
        }

        // 占位
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun PeriodSelector(
    period: TimePeriod,
    year: Int,
    month: Int,
    onPeriodSelected: (TimePeriod) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        // 周/月/年切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimePeriod.entries.forEach { p ->
                FilterChip(
                    selected = period == p,
                    onClick = { onPeriodSelected(p) },
                    label = {
                        Text(
                            when (p) {
                                TimePeriod.WEEK -> "周"
                                TimePeriod.MONTH -> "月"
                                TimePeriod.YEAR -> "年"
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 月份前后切换
        if (period != TimePeriod.YEAR) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一月")
                }
                Text(
                    "${year}年${month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一月")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一年")
                }
                Text(
                    "${year}年",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一年")
                }
            }
        }
    }
}

@Composable
fun SummaryCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("收入", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    DateUtils.formatAmount(income),
                    color = IncomeColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            VerticalDivider(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支出", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    DateUtils.formatAmount(expense),
                    color = ExpenseColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            VerticalDivider(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("结余", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    DateUtils.formatAmount(income - expense),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryStatItem(
    name: String,
    amount: Double,
    percentage: Double,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色圆点
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(Modifier.width(12.dp))

            // 分类名
            Text(
                name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            // 百分比
            Text(
                String.format("%.1f%%", percentage),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.width(60.dp)
            )

            // 金额
            Text(
                DateUtils.formatAmount(amount),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }

        // 进度条
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 12.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(8.dp))
    }
}
