package cn.fkj233.deviceemulator.app.ui.common.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission(title: String, vararg permission: String) {
    val permissionState = rememberMultiplePermissionsState(
        permission.toList()
    )

    val context = LocalContext.current

    if (!permissionState.allPermissionsGranted) {
        AlertDialog(
            onDismissRequest = {  },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                ) {
                    Text(text = "去设置")
                }
                Button(
                    onClick = {
                        permissionState.launchMultiplePermissionRequest()
                    }
                ) {
                    Text(text = "同意")
                }
            },
            text = {
                Text(text = "本程序功能需要获得\"$title\"权限\n您若拒绝本程序将无法运行！")
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permission(
    modifier: Modifier = Modifier
) {
    var permissionRequested by remember { mutableStateOf(false) }

    var shouldOpenLocationSettings by remember { mutableStateOf(false) }
    var shouldShowRationale by remember { mutableStateOf(false) }
    var shouldShowAppSettings by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) {
        Log.i("Permission", "State = $it")
        if (permissionRequested && shouldShowAppSettings)
            shouldShowAppSettings = true

        if (permissionRequested)
            shouldShowRationale = true

        permissionRequested = true
    }

    val context = LocalContext.current

    // 在 composable 中重新检查权限
    LaunchedEffect(Unit) {
        // 每次 composable 被重新显示时执行的代码
        if (permissionState.status.isGranted) {
            // 定位逻辑
            Log.i("Permission", "定位逻辑")
        } else {
            if (permissionState.status.shouldShowRationale) {
                Log.i("Permission", "shouldShowRationale")
                shouldShowRationale = true
            } else {
                if (!permissionRequested) {
                    permissionState.launchPermissionRequest()
                } else {
                    shouldShowAppSettings = true
                }
            }
        }
    }

    // 其余的逻辑保持不变

    if (shouldOpenLocationSettings) {
        AlertDialog(
            onDismissRequest = { shouldOpenLocationSettings = false },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        } catch (e: Exception) {
                            context.startActivity(
                                Intent(Settings.ACTION_SETTINGS)
                            )
                        }
                    }
                ) {
                    Text(text = "设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { shouldOpenLocationSettings = false }) {
                    Text(text = "取消")
                }
            },
            text = {
                Text(text = "您的系统设置没有开启【位置信息】，请前往设置界面开启")
            }
        )
    }

    if (shouldShowAppSettings) {
        AlertDialog(
            onDismissRequest = { shouldShowAppSettings = false },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                        shouldShowAppSettings = false
                    }
                ) {
                    Text(text = "去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { shouldShowAppSettings = false }) {
                    Text(text = "取消")
                }
            },
            text = {
                Text(text = "您已拒绝授予定位权限，请前往设置界面授权")
            }
        )
    }

    if (shouldShowRationale) {
        AlertDialog(
            onDismissRequest = { shouldShowRationale = false },
            confirmButton = {
                Button(
                    onClick = {
                        shouldShowRationale = false
                        permissionState.launchPermissionRequest()
                    }
                ) {
                    Text(text = "授权")
                }
            },
            dismissButton = {
                TextButton(onClick = { shouldShowRationale = false }) {
                    Text(text = "取消")
                }
            },
            text = {
                Text(text = "定位需要您授予权限才嗯能使用")
            }
        )
    }
}
