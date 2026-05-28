package com.example.accounting.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.navigation.Screen
import com.example.accounting.ui.theme.ExpenseColor
import com.example.accounting.ui.theme.IncomeColor
import com.example.accounting.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI记账") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Budget.route) }) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "预算")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 月度概览卡片
            item {
                MonthSummaryCard(
                    income = uiState.monthIncome,
                    expense = uiState.monthExpense
                )
            }

        // 今日账单
        item {
            Text(
                "今日账单",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (uiState.todayTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("今天还没有记账哦", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(uiState.todayTransactions, key = { "today_${it.id}" }) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = { navController.navigate(Screen.EditTransaction.createRoute(transaction.id)) },
                onDelete = { viewModel.deleteTransaction(transaction) }
            )
        }

        // 最近账单
        if (uiState.recentTransactions.isNotEmpty()) {
            item {
                Text(
                    "最近账单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            val todayIds = uiState.todayTransactions.map { it.id }.toSet()
            val recentOnly = uiState.recentTransactions.filter { it.id !in todayIds }.take(10)
            items(recentOnly, key = { "recent_${it.id}" }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { navController.navigate(Screen.EditTransaction.createRoute(transaction.id)) },
                    onDelete = { viewModel.deleteTransaction(transaction) }
                )
            }
        }
        }
    }
}

@Composable
fun MonthSummaryCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    DateUtils.formatMonth(System.currentTimeMillis()),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "结余 ${DateUtils.formatAmount(income - expense)}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("收入", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            DateUtils.formatAmount(income),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("支出", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            DateUtils.formatAmount(expense),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (transaction.type == TransactionType.EXPENSE)
                            ExpenseColor.copy(alpha = 0.1f)
                        else
                            IncomeColor.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (transaction.type == TransactionType.EXPENSE) Icons.Default.ShoppingCart else Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.EXPENSE) ExpenseColor else IncomeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // 商户/商品（主标题）和分类（副标题）
            Column(modifier = Modifier.weight(1f)) {
                // 主标题：商户/商品
                Text(
                    transaction.note.ifEmpty { transaction.categoryName.ifEmpty { "未分类" } },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                // 副标题：分类 + 日期
                Row {
                    Text(
                        transaction.categoryName.ifEmpty { "未分类" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        DateUtils.formatDateTime(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 照片标识
                if (!transaction.imagePath.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "含票据照片",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 金额
            Text(
                "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${DateUtils.formatAmount(transaction.amount)}",
                color = if (transaction.type == TransactionType.EXPENSE) ExpenseColor else IncomeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // 删除按钮
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
