package cn.fkj233.deviceemulator.app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import cn.fkj233.deviceemulator.app.ui.screen.Home
import cn.fkj233.deviceemulator.app.ui.screen.MockLocation
import cn.fkj233.deviceemulator.app.ui.screen.Setting

typealias NavAction = (String, List<Pair<String, Any>>) -> Unit

data class BottomItemData(val route: String, val label: String, val icon: ImageVector, val title: String = label, val customRoute: (NavGraphBuilder.(BottomItemData, NavAction) -> Unit)? = null, val content: @Composable (NavAction) -> Unit = {})

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEmulatorApp() {
    val navController = rememberNavController()

    val menuData = listOf(
        BottomItemData("Home", "首页", Icons.Filled.Home, "DeviceEmulator"){ Home() },
        BottomItemData("MockLocation", "模拟位置", Icons.Filled.LocationOn, customRoute = { item, action ->
            navigation(startDestination = "MockInfo", route = item.route) {
                composable("MockInfo") {
                    MockLocation(action)
                }
                composable("SelectLocation") {

                }
            }
        }),
        BottomItemData("Settings", "设置", Icons.Filled.Settings) { Setting() }
    )


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = menuData.find { it.route == currentDestination?.hierarchy?.first()?.route }?.title ?: "")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            if (currentDestination?.hierarchy?.any { his -> menuData.any { it.route == his.route } } == false) return@Scaffold
            NavigationBar {
                menuData.forEach { bottomItemData ->
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
                                contentDescription = "按钮"
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
            NavHost(navController = navController, startDestination = menuData[1].route) {
                val navAction = { route: String, args: List<Pair<String, Any>> ->
                    val value = args.joinToString("&") { it.first + "=" + it.second }

                    val fullRoute = when {
                        args.isEmpty() -> route
                        route.contains("?") -> "{$route}$value"
                        else -> "$route?$value"
                    }
                    //拼接路由和参数列表,形成真正的路由
                    Log.d("starsone", "MyApp跳转路径: $fullRoute")

                    //调用方法进行页面跳转
                    navController.navigate(fullRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                menuData.forEach { item ->
                    if (item.customRoute != null) {
                        item.customRoute.invoke(this, item, navAction)
                    } else {
                        composable(item.route) {
                            item.content(navAction)
                        }
                    }
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