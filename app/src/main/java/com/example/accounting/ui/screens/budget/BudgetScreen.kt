package com.example.accounting.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.accounting.data.db.entity.Budget
import com.example.accounting.ui.theme.AppTokens
import com.example.accounting.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算管理") },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加预算")
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
            // 月份选择器
            item {
                MonthSelector(
                    year = uiState.year,
                    month = uiState.month,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
            }

            // 总预算卡片
            item {
                TotalBudgetCard(
                    budget = uiState.totalBudget,
                    spent = uiState.totalSpent,
                    onEdit = { viewModel.showEditDialog(it) },
                    onAdd = { viewModel.showAddDialog(true) }
                )
            }

            // 分类预算标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("分类预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.showAddDialog(false) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("添加")
                    }
                }
            }

            // 分类预算列表
            if (uiState.categoryBudgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无分类预算", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(uiState.categoryBudgets) { item ->
                CategoryBudgetCard(
                    item = item,
                    onEdit = { viewModel.showEditDialog(item.budget) },
                    onDelete = { viewModel.deleteBudget(item.budget) }
                )
            }
        }
    }

    // 添加/编辑对话框
    if (uiState.showAddDialog) {
        BudgetDialog(
            uiState = uiState,
            onAmountChange = viewModel::updateAmount,
            onCategorySelect = viewModel::selectCategory,
            onConfirm = viewModel::saveBudget,
            onDismiss = viewModel::dismissDialog
        )
    }
}

@Composable
fun MonthSelector(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上月")
        }
        Text(
            "${year}年${month}月",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下月")
        }
    }
}

@Composable
fun TotalBudgetCard(
    budget: Budget?,
    spent: Double,
    onEdit: (Budget) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("本月总预算", style = MaterialTheme.typography.titleSmall)
                if (budget != null) {
                    IconButton(onClick = { onEdit(budget) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (budget != null) {
                val remaining = budget.amount - spent
                val percentage = if (budget.amount > 0) (spent / budget.amount * 100) else 0.0

                Text(
                    DateUtils.formatAmount(budget.amount),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(12.dp))

                // 进度条
                LinearProgressIndicator(
                    progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (percentage > 90) AppTokens.errorColor() else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("已花 ${DateUtils.formatAmount(spent)}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "剩余 ${DateUtils.formatAmount(remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining < 0) AppTokens.errorColor() else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                OutlinedButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("设置本月预算")
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(
    item: BudgetItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个预算吗？") },
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.budget.categoryName.ifEmpty { "未分类" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "预算 ${DateUtils.formatAmount(item.budget.amount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (item.percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (item.percentage > 90) AppTokens.errorColor() else MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "已花 ${DateUtils.formatAmount(item.spent)} / 剩余 ${DateUtils.formatAmount(item.remaining)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BudgetDialog(
    uiState: BudgetUiState,
    onAmountChange: (String) -> Unit,
    onCategorySelect: (com.example.accounting.data.db.entity.Category) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (uiState.editingBudget != null) "编辑预算" else "添加预算")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.newBudgetAmount,
                    onValueChange = onAmountChange,
                    label = { Text("预算金额") },
                    prefix = { Text("¥ ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!uiState.isTotalBudget) {
                    Text("选择分类", style = MaterialTheme.typography.bodyMedium)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(uiState.categories) { category ->
                            val isSelected = category.id == uiState.selectedCategory?.id
                            OutlinedButton(
                                onClick = { onCategorySelect(category) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(category.name, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = uiState.newBudgetAmount.toDoubleOrNull() != null
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
