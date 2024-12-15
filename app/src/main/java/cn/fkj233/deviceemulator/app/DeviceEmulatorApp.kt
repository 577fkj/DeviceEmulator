package cn.fkj233.deviceemulator.app

import android.Manifest
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.fkj233.deviceemulator.app.ui.screen.*
import cn.fkj233.deviceemulator.app.ui.utils.RequestPermission

data class BottomItemData(val route: String, val label: String, val icon: ImageVector, val title: String = label, val content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEmulatorApp() {
    RequestPermission("定位权限", Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    val navController = rememberNavController()

    val menuData = listOf(
        BottomItemData("Home", "首页", Icons.Filled.Home, "DeviceEmulator"){ Home() },
        BottomItemData("Settings", "设置", Icons.Filled.Settings) { Setting() }
    )


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = menuData.find { it.route == currentDestination?.hierarchy?.first()?.route }?.title ?: "")
                }
            )
        },
        bottomBar = {
            NavigationBar {
                menuData.forEachIndexed { index, bottomItemData ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == bottomItemData.route } == true,
                        onClick = {
                            navController.navigate(bottomItemData.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = bottomItemData.icon,
                                contentDescription = "点击按钮"
                            )
                        },
                        label = {
                            Text(
                                text = (bottomItemData.label)
                            )
                        },
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = menuData[0].route) {
                menuData.forEach { item ->
                    composable(item.route, content = item.content)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceEmulatorAppPreview() {
    DeviceEmulatorApp()
}