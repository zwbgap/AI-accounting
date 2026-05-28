package com.example.accounting.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.accounting.ai.AiSettingsManager
import com.example.accounting.ui.screens.add.AddTransactionScreen
import com.example.accounting.ui.screens.ai.AiSettingsScreen
import com.example.accounting.ui.screens.analysis.AnalysisScreen
import com.example.accounting.ui.screens.budget.BudgetScreen
import com.example.accounting.ui.screens.camera.CameraScreen
import com.example.accounting.ui.screens.category.CategoryScreen
import com.example.accounting.ui.screens.home.HomeScreen
import com.example.accounting.ui.screens.search.SearchScreen
import com.example.accounting.ui.screens.settings.SettingsScreen
import com.example.accounting.ui.screens.voice.VoiceScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Analysis : Screen("analysis", "分析", Icons.Default.PieChart)
    data object Add : Screen("add", "记账", Icons.Default.Add)
    data object Category : Screen("category", "分类", Icons.Default.Category)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
    data object Camera : Screen("camera", "拍照识别", Icons.Default.CameraAlt)
    data object Voice : Screen("voice", "AI智能记账", Icons.Default.AutoAwesome)
    data object Budget : Screen("budget", "预算", Icons.Default.AccountBalance)
    data object Search : Screen("search", "搜索", Icons.Default.Search)
    data object AiSettings : Screen("ai_settings", "AI设置", Icons.Default.SmartToy)
    data object EditTransaction : Screen("edit_transaction/{id}", "编辑", Icons.Default.Edit) {
        fun createRoute(id: Long) = "edit_transaction/$id"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Analysis, Screen.Add, Screen.Category, Screen.Settings)

private const val NAV_ANIM_DURATION = 200

private val bottomNavEnterTransition: EnterTransition =
    fadeIn(tween(NAV_ANIM_DURATION))

private val bottomNavExitTransition: ExitTransition =
    fadeOut(tween(NAV_ANIM_DURATION))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val aiSettingsManager = remember { AiSettingsManager(context) }

    var showAddOptionsSheet by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    // 添加选项底部弹窗
    if (showAddOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "AI智能记账",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 拍照记账 - 直接启动相机
                ListItem(
                    headlineContent = { Text("拍照记账") },
                    supportingContent = { Text("拍摄票据自动识别金额") },
                    leadingContent = {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showAddOptionsSheet = false
                        navController.navigate("camera?autoLaunch=true")
                    }
                )

                HorizontalDivider()

                // 相册识别 - 直接打开相册
                ListItem(
                    headlineContent = { Text("相册识别") },
                    supportingContent = { Text("从相册选择票据图片识别") },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showAddOptionsSheet = false
                        navController.navigate("camera?source=gallery")
                    }
                )

                HorizontalDivider()

                // AI智能记账
                ListItem(
                    headlineContent = { Text("AI智能记账") },
                    supportingContent = { Text("自然语言输入，AI自动解析") },
                    leadingContent = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showAddOptionsSheet = false
                        navController.navigate(Screen.Voice.route)
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        if (screen == Screen.Add) {
                            // 记账按钮 - 使用Box + combinedClickable处理单击和长按
                            NavigationBarItem(
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .combinedClickable(
                                                onClick = {
                                                    // 单击 - 直接进入手动记账
                                                    navController.navigate(Screen.Add.route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                                onLongClick = {
                                                    // 长按 - 显示选项
                                                    showAddOptionsSheet = true
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = screen.title,
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = { /* 由combinedClickable处理 */ }
                            )
                        } else {
                            // 其他导航项
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                Screen.Home.route,
                enterTransition = { bottomNavEnterTransition },
                exitTransition = { bottomNavExitTransition }
            ) { HomeScreen(navController) }
            composable(
                Screen.Analysis.route,
                enterTransition = { bottomNavEnterTransition },
                exitTransition = { bottomNavExitTransition }
            ) { AnalysisScreen() }
            composable(
                Screen.Add.route,
                enterTransition = { bottomNavEnterTransition },
                exitTransition = { bottomNavExitTransition }
            ) { AddTransactionScreen(navController) }
            composable(
                Screen.Category.route,
                enterTransition = { bottomNavEnterTransition },
                exitTransition = { bottomNavExitTransition }
            ) { CategoryScreen() }
            composable(
                Screen.Settings.route,
                enterTransition = { bottomNavEnterTransition },
                exitTransition = { bottomNavExitTransition }
            ) { SettingsScreen(navController) }
            composable(Screen.Camera.route) { CameraScreen(navController) }
            composable("camera?source={source}") { backStackEntry ->
                val source = backStackEntry.arguments?.getString("source")
                CameraScreen(navController, initialSource = source)
            }
            composable("camera?autoLaunch={autoLaunch}") { backStackEntry ->
                val autoLaunch = backStackEntry.arguments?.getString("autoLaunch") == "true"
                CameraScreen(navController, autoLaunchCamera = autoLaunch)
            }
            composable(Screen.Voice.route) { VoiceScreen(navController) }
            composable(Screen.Budget.route) { BudgetScreen() }
            composable(Screen.Search.route) { SearchScreen(navController) }
            composable(Screen.AiSettings.route) { AiSettingsScreen(navController) }
            composable("edit_transaction/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                AddTransactionScreen(navController, editId = id)
            }
        }
    }
}
