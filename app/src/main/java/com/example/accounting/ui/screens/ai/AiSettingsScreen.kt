package com.example.accounting.ui.screens.ai

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    navController: NavController,
    viewModel: AiSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showApiUrlDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AI总开关
            item {
                SettingsSection("AI功能") {
                    SettingsSwitchItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI功能总开关",
                        subtitle = "开启后可使用所有AI功能",
                        checked = uiState.isAiEnabled,
                        onCheckedChange = viewModel::setAiEnabled
                    )
                }
            }

            // API配置
            item {
                SettingsSection("API配置") {
                    // API Key
                    SettingsClickableItem(
                        icon = Icons.Default.Key,
                        title = "API Key",
                        subtitle = if (uiState.apiKey.isNotBlank()) "已配置 (${uiState.apiKey.take(8)}...)" else "未配置",
                        onClick = { showApiKeyDialog = true }
                    )

                    // 供应商选择
                    var providerExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { providerExpanded = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("供应商", style = MaterialTheme.typography.bodyLarge)
                            Text(uiState.selectedProvider, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DropdownMenu(
                            expanded = providerExpanded,
                            onDismissRequest = { providerExpanded = false }
                        ) {
                            aiProviders.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider.name) },
                                    onClick = {
                                        viewModel.selectProvider(provider.name)
                                        providerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // API地址（始终可编辑）
                    SettingsClickableItem(
                        icon = Icons.Default.Link,
                        title = "API地址",
                        subtitle = uiState.apiUrl.take(40) + if (uiState.apiUrl.length > 40) "..." else "",
                        onClick = { showApiUrlDialog = true }
                    )

                    // 模型名称（始终可编辑）
                    SettingsClickableItem(
                        icon = Icons.Default.Language,
                        title = "模型名称",
                        subtitle = uiState.model,
                        onClick = { showModelDialog = true }
                    )

                    // 测试连接按钮
                    Button(
                        onClick = viewModel::testConnection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !uiState.isTesting && uiState.apiKey.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("测试中...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("测试连接")
                        }
                    }

                    // 测试结果
                    uiState.testResult?.let { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.testSuccess == true)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (uiState.testSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (uiState.testSuccess == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    result,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.testSuccess == true)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // AI子功能开关
            item {
                SettingsSection("AI子功能") {
                    SettingsSwitchItem(
                        icon = Icons.Default.CameraAlt,
                        title = "图片识别",
                        subtitle = "拍照或选择图片，AI识别账单",
                        checked = uiState.isOcrEnabled,
                        onCheckedChange = viewModel::setOcrEnabled,
                        enabled = uiState.isAiEnabled
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "自然语言解析",
                        subtitle = "输入自然语言，AI自动解析账单",
                        checked = uiState.isVoiceEnabled,
                        onCheckedChange = viewModel::setVoiceEnabled,
                        enabled = uiState.isAiEnabled
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Category,
                        title = "AI智能分类",
                        subtitle = "自动识别消费分类",
                        checked = uiState.isAiCategoryEnabled,
                        onCheckedChange = viewModel::setAiCategoryEnabled,
                        enabled = uiState.isAiEnabled
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.TextFields,
                        title = "AI文本解析",
                        subtitle = "自然语言记账解析",
                        checked = uiState.isAiParseEnabled,
                        onCheckedChange = viewModel::setAiParseEnabled,
                        enabled = uiState.isAiEnabled
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Analytics,
                        title = "AI消费建议",
                        subtitle = "智能分析消费并提供建议",
                        checked = uiState.isAiAdviceEnabled,
                        onCheckedChange = viewModel::setAiAdviceEnabled,
                        enabled = uiState.isAiEnabled
                    )
                }
            }

            // 说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("说明", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "• AI功能需要联网使用API服务\n" +
                                    "• 图片识别需要支持视觉的模型（如glm-4v）\n" +
                                    "• 自然语言解析支持中文输入\n" +
                                    "• 所有账单分类完全由AI决定\n" +
                                    "• 支持多种AI供应商，可自由切换",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // API Key配置对话框
    if (showApiKeyDialog) {
        var tempApiKey by remember { mutableStateOf(uiState.apiKey) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("配置API Key") },
            text = {
                Column {
                    Text(
                        "请输入API Key",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("sk-...") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateApiKey(tempApiKey)
                    showApiKeyDialog = false
                    Toast.makeText(context, "API Key已保存", Toast.LENGTH_SHORT).show()
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // API URL配置对话框
    if (showApiUrlDialog) {
        var tempApiUrl by remember { mutableStateOf(uiState.apiUrl) }

        AlertDialog(
            onDismissRequest = { showApiUrlDialog = false },
            title = { Text("配置API地址") },
            text = {
                Column {
                    Text(
                        "请输入API地址",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempApiUrl,
                        onValueChange = { tempApiUrl = it },
                        label = { Text("API URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://api.example.com/v1/chat/completions") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "支持OpenAI兼容格式的API地址",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateApiUrl(tempApiUrl)
                    showApiUrlDialog = false
                    Toast.makeText(context, "API地址已保存", Toast.LENGTH_SHORT).show()
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiUrlDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 模型名称配置对话框
    if (showModelDialog) {
        var tempModel by remember { mutableStateOf(uiState.model) }

        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = { Text("配置模型名称") },
            text = {
                Column {
                    Text(
                        "请输入模型名称",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempModel,
                        onValueChange = { tempModel = it },
                        label = { Text("模型名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("glm-4-flash") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "常用模型：glm-4-flash, glm-4, gpt-4o, gpt-3.5-turbo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateModelName(tempModel)
                    showModelDialog = false
                    Toast.makeText(context, "模型名称已保存", Toast.LENGTH_SHORT).show()
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModelDialog = false }) {
                    Text("取消")
                }
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
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
