package com.example.accounting.ui.screens.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.ui.screens.add.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    navController: NavController,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 保存成功后返回
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI智能记账") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 提示文本
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("输入记账内容，AI自动解析", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    Text("例如：\"午饭花了35块\" \"收到工资8000元\"", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // 文字输入
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::updateInputText,
                label = { Text("输入记账内容") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("例如：打车花了20块") }
            )

            // 解析按钮
            Button(
                onClick = viewModel::parseFromText,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.inputText.isNotBlank() && !uiState.isParsing
            ) {
                if (uiState.isParsing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                }
                Text("AI智能解析")
            }

            // 解析中
            if (uiState.isParsing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // 解析结果
            if (uiState.showResult) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("解析结果", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            // 类型标签
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (uiState.parsedType == TransactionType.INCOME)
                                    MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = if (uiState.parsedType == TransactionType.INCOME) "收入" else "支出",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = if (uiState.parsedType == TransactionType.INCOME)
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        HorizontalDivider()

                        OutlinedTextField(
                            value = uiState.parsedAmount,
                            onValueChange = viewModel::updateAmount,
                            label = { Text("金额") },
                            prefix = { Text("¥ ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.errorMessage != null,
                            supportingText = uiState.errorMessage?.let { { Text(it) } }
                        )

                        // AI智能分类选择
                        Text("分类", style = MaterialTheme.typography.bodyMedium)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.heightIn(max = 150.dp)
                        ) {
                            items(uiState.categories) { category ->
                                val isSelected = category.id == uiState.selectedCategory?.id
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectCategory(category) }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        getCategoryIcon(category.icon),
                                        contentDescription = category.name,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        category.name,
                                        fontSize = 9.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.parsedNote,
                            onValueChange = viewModel::updateNote,
                            label = { Text("备注") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // 确认保存按钮
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("确认记账", fontSize = 16.sp)
                }
            }

            // 错误信息
            if (uiState.errorMessage != null && !uiState.showResult) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        uiState.errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // 底部间距
            Spacer(Modifier.height(16.dp))
        }
    }
}
