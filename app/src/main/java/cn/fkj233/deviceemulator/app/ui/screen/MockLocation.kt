package cn.fkj233.deviceemulator.app.ui.screen

import android.Manifest
import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.fkj233.deviceemulator.app.NavAction
import cn.fkj233.deviceemulator.app.manager.DeviceEmulatorManager
import cn.fkj233.deviceemulator.app.pref.AddressData
import cn.fkj233.deviceemulator.app.pref.AddressInfo
import cn.fkj233.deviceemulator.app.pref.add
import cn.fkj233.deviceemulator.app.pref.convertToLocation
import cn.fkj233.deviceemulator.app.pref.remove
import cn.fkj233.deviceemulator.app.ui.common.launcher.handlerGPSLauncher
import cn.fkj233.deviceemulator.app.ui.common.utils.requestMultiplePermission
import cn.fkj233.deviceemulator.app.ui.common.utils.showToast
import cn.fkj233.deviceemulator.app.ui.contract.MockLocationContract
import cn.fkj233.deviceemulator.app.ui.viewmodel.MockLocationViewModel
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.position.rememberCameraPositionState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun ShowOpenGPSDialog(onPositiveClick: () -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("请打开GPS权限")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onPositiveClick
                    ) {
                        Text("打开")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MockLocation(navAction: NavAction) {
    val viewModel: MockLocationViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val cameraPosition = rememberCameraPositionState {
        // 不预加载显示默认北京的位置
        position = CameraPosition(LatLng(0.0, 0.0), 18f, 0f, 0f)
    }
    val historyLocation = remember { mutableStateListOf(*AddressData.historyAddress.list.toTypedArray()) }
    val service = DeviceEmulatorManager.getDefault().getMockLocationService()

    viewModel.setMockLocationStatus(service.mockStatus)

    viewModel.setMapDarkMode(isSystemInDarkTheme())

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            when (it) {
                is MockLocationContract.Effect.ShowToast -> {
                    showToast(it.msg)
                    Log.d("DeviceEmulator", "MockLocation: ${it.msg}")
                }
                is MockLocationContract.Effect.AddLocation -> {
                    val data = AddressInfo(it.position.lat, it.position.lng, it.position.address)
                    historyLocation.add(data)
                    AddressData.historyAddress.add(data)
                }
                is MockLocationContract.Effect.RemoveLocation -> {
                    historyLocation.removeAt(it.index)
                    AddressData.historyAddress.remove(it.index)
                }
                is MockLocationContract.Effect.MockStatusChanged -> {
                    service.mockLocation = currentState.position?.toAddressInfo()?.convertToLocation()
                    service.mockStatus = it.isMocking
                }
            }
        }.collect()
    }

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)
    val reqGPSPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onGrantAllPermission = viewModel::handleGrantLocationPermission,
        onNoGrantPermission = viewModel::handleNoGrantLocationPermission
    )

    LaunchedEffect(Unit) {
        snapshotFlow { reqGPSPermission.allPermissionsGranted }.collect {
            // 从app应用权限开关页面，打开权限，需要再检查一下GPS开关
            viewModel.checkGpsStatus()
        }
    }

    LaunchedEffect(currentState.locationLatLng) {
        if(null == currentState.locationLatLng) return@LaunchedEffect
        cameraPosition.move(CameraUpdateFactory.newLatLng(currentState.locationLatLng))
    }

    LaunchedEffect(currentState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(currentState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            } else {
                viewModel.startMapLocation()
            }
        }
    }

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    navAction.ObserverBackData<MockLocationContract.Position?>("pos") {
        if (it != null) {
            viewModel.setPosition(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp, top = 5.dp)
    ) {
        GDMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(14.dp)),
            cameraPositionState = cameraPosition,
            properties = currentState.mapProperties,
            uiSettings = currentState.mapUiSettings,
            locationSource = viewModel,
            onMapLoaded = viewModel::checkGpsStatus
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    navAction.navigate(":SelectLocation")
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .wrapContentSize()
            ) {
                Text("模拟位置",
                    fontSize = 12.sp)

                val address = if (!currentState.position?.address.isNullOrEmpty()) {
                    "${currentState.position?.address}"
                } else {
                    "未知"
                }

                val latLng = if (currentState.position != null) {
                    "${currentState.position?.lat},${currentState.position?.lng}"
                } else {
                    "未知"
                }

                Row {
                    Text("目标：",
                        fontSize = 16.sp)
                    Text(
                        address,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .basicMarquee(),
                        maxLines = 1
                    )
                }

                Row {
                    Text("经纬度：",
                        fontSize = 12.sp)
                    Text(
                        latLng,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .basicMarquee(),
                        maxLines = 1
                    )
                }

                Button(modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(40.dp),
                    onClick = viewModel::switchMockLocationStatus
                ) {
                    if (currentState.isMocking) {
                        Text("停止模拟")
                    } else {
                        Text("启动模拟")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用摇杆：",
                            fontSize = 14.sp)
                        Switch(
                            checked = false,
                            onCheckedChange = {

                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("模拟速度：",
                            fontSize = 14.sp)
                        Switch(
                            checked = false,
                            onCheckedChange = {

                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text("历史定位 (${historyLocation.size})",
                    fontSize = 16.sp)
                IconButton(
                    onClick = {
                        navAction.navigate(":SelectLocation")
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "增加")
                }
            }

            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(scroll)
            ) {
                historyLocation.forEachIndexed { index, info ->
                    AddressItem(index, info, viewModel)
                }
            }
        }
    }
}

@Composable
fun AddressItem(index: Int, info: AddressInfo, viewModel: MockLocationViewModel) {
    Card(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(10f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "地址：",
                        fontSize = 16.sp
                    )
                    Text(
                        info.address,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .basicMarquee(),
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "经纬度：",
                        fontSize = 12.sp
                    )
                    Text(
                        "${info.latitude}, ${info.longitude}",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .basicMarquee(),
                        maxLines = 1
                    )
                }
            }
            IconButton(
                onClick = {
                    viewModel.removePosition(index)
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "删除")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMockLocation() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        AddressItem(0, AddressInfo(0.0, 0.0, "北京市"), viewModel())
    }
}