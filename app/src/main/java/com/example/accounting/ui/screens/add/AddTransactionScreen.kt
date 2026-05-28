package com.example.accounting.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.ui.theme.ExpenseColor
import com.example.accounting.ui.theme.IncomeColor
import com.example.accounting.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    editId: Long? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(editId) {
        if (editId != null && editId > 0) {
            viewModel.loadTransaction(editId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "编辑账单" else "记一笔") },
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
            // 收入/支出切换
            TypeSelector(
                selectedType = uiState.type,
                onTypeSelected = viewModel::updateType
            )

            // 金额输入
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("金额") },
                prefix = { Text("¥ ", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                isError = uiState.errorMessage != null,
                supportingText = uiState.errorMessage?.let { { Text(it) } },
                singleLine = true
            )

            // 分类选择
            Text("选择分类", style = MaterialTheme.typography.titleSmall)
            CategoryGrid(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )

            // 备注
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 日期选择
            DatePickerRow(
                date = uiState.date,
                onDateSelected = viewModel::updateDate
            )

            Spacer(Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selectedType
            val color = if (type == TransactionType.EXPENSE) ExpenseColor else IncomeColor

            OutlinedButton(
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                    contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).let {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) color else MaterialTheme.colorScheme.outline
                    )
                }
            ) {
                Text(
                    if (type == TransactionType.EXPENSE) "支出" else "收入",
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategory?.id
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCategorySelected(category) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    getCategoryIcon(category.icon),
                    contentDescription = category.name,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    category.name,
                    fontSize = 10.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerRow(
    date: Long,
    onDateSelected: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA) }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(dateFormat.format(Date(date)))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun getCategoryIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "sports_esports" -> Icons.Default.SportsEsports
        "home" -> Icons.Default.Home
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "phone" -> Icons.Default.Phone
        "checkroom" -> Icons.Default.Checkroom
        "payments" -> Icons.Default.Payments
        "emoji_events" -> Icons.Default.EmojiEvents
        "trending_up" -> Icons.Default.TrendingUp
        "work" -> Icons.Default.Work
        else -> Icons.Default.MoreHoriz
    }
}
