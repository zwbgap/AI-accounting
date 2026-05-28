package com.example.accounting.ui.screens.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.ui.screens.add.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var showRemoveDuplicatesDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置分类") },
            text = { Text("将删除所有自定义分类，恢复默认分类列表") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToDefault()
                    showResetDialog = false
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("取消") }
            }
        )
    }

    if (showRemoveDuplicatesDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDuplicatesDialog = false },
            title = { Text("清理重复分类") },
            text = { Text("将删除重复的分类，保留每个分类的第一个") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeDuplicates()
                    showRemoveDuplicatesDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDuplicatesDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                actions = {
                    IconButton(onClick = { showRemoveDuplicatesDialog = true }) {
                        Icon(Icons.Default.CleaningServices, contentDescription = "清理重复")
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    IconButton(onClick = viewModel::showAddDialog) {
                        Icon(Icons.Default.Add, contentDescription = "添加分类")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // 类型切换
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.selectedType == type,
                        onClick = { viewModel.updateType(type) },
                        label = { Text(if (type == TransactionType.EXPENSE) "支出分类" else "收入分类") }
                    )
                }
            }

            // 分类列表
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getCategoryIcon(category.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                category.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = { viewModel.showEditDialog(category) }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(20.dp))
                            }
                            if (!category.isDefault) {
                                IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 添加/编辑对话框
    if (uiState.showAddDialog) {
        CategoryDialog(
            name = uiState.newCategoryName,
            icon = uiState.newCategoryIcon,
            isEditing = uiState.editingCategory != null,
            onNameChange = viewModel::updateNewName,
            onIconChange = viewModel::updateNewIcon,
            onConfirm = viewModel::saveCategory,
            onDismiss = viewModel::dismissDialog
        )
    }
}

@Composable
fun CategoryDialog(
    name: String,
    icon: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val iconOptions = listOf(
        "restaurant", "directions_car", "shopping_bag", "sports_esports",
        "home", "local_hospital", "school", "phone", "checkroom",
        "payments", "emoji_events", "trending_up", "work", "more_horiz"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑分类" else "添加分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("选择图标", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions) { iconName ->
                        val isSelected = iconName == icon
                        IconButton(
                            onClick = { onIconChange(iconName) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                getCategoryIcon(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
