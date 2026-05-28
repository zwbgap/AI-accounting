package com.example.accounting.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.accounting.data.repository.TransactionRepository
import com.example.accounting.navigation.Screen
import com.example.accounting.util.CsvExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSection("数据管理") {
                    SettingsItem(
                        icon = Icons.Default.FileDownload,
                        title = "导出数据",
                        subtitle = if (uiState.isExporting) "导出中..." else "导出账单为CSV文件",
                        onClick = {
                            if (!uiState.isExporting) {
                                viewModel.exportData(context)
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.AccountBalance,
                        title = "预算管理",
                        subtitle = "设置月度预算",
                        onClick = { navController.navigate(Screen.Budget.route) }
                    )
                }
            }

            item {
                SettingsSection("AI设置") {
                    SettingsItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI功能设置",
                        subtitle = "配置API Key、开关AI功能",
                        onClick = { navController.navigate(Screen.AiSettings.route) }
                    )
                }
            }

            item {
                SettingsSection("关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于AI记账",
                        subtitle = "版本 1.0",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // 导出成功提示
    LaunchedEffect(uiState.exportResult) {
        uiState.exportResult?.let { result ->
            result.fold(
                onSuccess = { path ->
                    Toast.makeText(context, "导出成功: $path", Toast.LENGTH_LONG).show()
                },
                onFailure = { e ->
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
            viewModel.clearExportResult()
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("AI智能记账") },
            text = {
                Column {
                    Text("一款集成AI功能的智能记账应用")
                    Spacer(Modifier.height(8.dp))
                    Text("功能特色：", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Text("• 拍照识别票据", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Text("• 语音/文本智能记账", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Text("• 消费分析与AI建议", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Text("• 预算管理", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Text("• 数据导出CSV", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Spacer(Modifier.height(8.dp))
                    Text("版本：1.0", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("确定") }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
