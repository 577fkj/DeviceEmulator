package cn.fkj233.deviceemulator.app

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination
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
import cn.fkj233.deviceemulator.app.ui.screen.SelectLocation
import cn.fkj233.deviceemulator.app.ui.screen.Setting

class NavAction(private val navController: NavController, private val menuData: List<BottomItemData>, private val currentDestination: NavDestination?) {
    fun getPageData(route: String? = getCurrentRoute()): BaseItemData? {
        val split = route?.split(":") ?: return null
        if (split.size == 1) {
            return menuData.find { it.route == route }
        }
        if (split[0] == split[1]) {
            return menuData.find { it.route == split[0] }
        }
        return menuData.find { it.route == split[0] }?.subPageData?.find { it.route == split[1] }
    }

    fun getCurrentRoute(): String? {
        return currentDestination?.hierarchy?.first()?.route
    }

    fun navigate(route: String, vararg args: Pair<String, Any>, clearStack: Boolean = false) {
        val value = args.joinToString("&") { it.first + "=" + it.second }

        val realRoute = if (route.startsWith(":")) {
            val mainRouteName = getCurrentRoute()?.split(":")?.get(0) ?: return
            "$mainRouteName$route"
        } else {
            route
        }

        val fullRoute = when {
            args.isEmpty() -> realRoute
            realRoute.contains("?") -> "$realRoute&$value"
            else -> "$realRoute?$value"
        }

        Log.d("DeviceEmulator", "DeviceEmulatorApp Route: $fullRoute")

        navController.navigate(fullRoute) {
            if (clearStack) {
                popUpTo(0) { inclusive = true }
            } else {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    fun popBackStack(vararg data: Pair<String, Any?>) {
        data.forEach { (key, value) ->
            navController.previousBackStackEntry?.savedStateHandle?.set(key, value)
        }
        navController.popBackStack()
    }

    fun <T> observerBackData(key: String, block: (T) -> Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)?.observeForever {
            block(it)
        }
    }
}

open class BaseItemData(open val route: String, open val label: String, open val title: String = label, val isMainPage: Boolean, open val content: @Composable (NavAction) -> Unit = {})

data class BottomItemData(override val route: String, override val label: String, val icon: ImageVector, override val title: String = label, val subPageData: ArrayList<SubItemPageData> = arrayListOf(), override val content: @Composable (NavAction) -> Unit = {}): BaseItemData(route, label, title, true, content)

data class SubItemPageData(override val route: String, override val label: String, override val title: String = label, override val content: @Composable (NavAction) -> Unit): BaseItemData(route, label, title, false, content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEmulatorApp() {
    val navController = rememberNavController()

    val menuData = listOf(
        BottomItemData("Home", "首页", Icons.Filled.Home, "DeviceEmulator"){ Home() },
        BottomItemData("MockLocation", "模拟位置", Icons.Filled.LocationOn, subPageData = arrayListOf(
            SubItemPageData("SelectLocation", "选择位置") { SelectLocation(it) }
        )) { MockLocation(it) },
        BottomItemData("Settings", "设置", Icons.Filled.Settings) { Setting() }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navAction = NavAction(navController, menuData, currentDestination)

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = navAction.getPageData()?.title ?: "")
                },
                navigationIcon = {
                    if (navAction.getPageData()?.isMainPage != false) return@TopAppBar
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
            if (navAction.getPageData()?.isMainPage == false) return@Scaffold
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
            NavHost(
                navController = navController,
                startDestination = menuData[1].route,
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) {
                menuData.forEach { item ->
                    if (item.subPageData.size > 0) {
                        navigation(startDestination = "${item.route}:${item.route}", route = item.route) {
                            composable("${item.route}:${item.route}") {
                                item.content(navAction)
                            }
                            item.subPageData.forEach { subPageData ->
                                composable("${item.route}:${subPageData.route}") {
                                    subPageData.content(navAction)
                                }
                            }
                        }
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