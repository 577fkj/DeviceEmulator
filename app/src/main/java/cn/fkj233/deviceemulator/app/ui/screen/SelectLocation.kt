package cn.fkj233.deviceemulator.app.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.fkj233.deviceemulator.R
import cn.fkj233.deviceemulator.app.NavAction
import cn.fkj233.deviceemulator.app.ui.common.utils.showToast
import cn.fkj233.deviceemulator.app.ui.contract.MockLocationContract
import cn.fkj233.deviceemulator.app.ui.contract.SelectLocationContract
import cn.fkj233.deviceemulator.app.ui.viewmodel.SelectLocationViewModel
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.model.MapType
import com.melody.map.gd_compose.overlay.Marker
import com.melody.map.gd_compose.overlay.rememberMarkerState
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.position.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun BoxScope.UIMarkerInScreenCenter(@DrawableRes resID: Int, dragDropAnimValueProvider: () -> Size) {
    Image(
        modifier = Modifier
            .align(Alignment.Center)
            .drawBehind {
                drawOval(
                    color = Color.Gray.copy(alpha = 0.7F),
                    topLeft = Offset(
                        size.width / 2 - dragDropAnimValueProvider().width / 2,
                        size.height / 2 - 18F
                    ),
                    size = dragDropAnimValueProvider()
                )
            }
            .graphicsLayer {
                translationY = -(dragDropAnimValueProvider().width.coerceAtLeast(5F) / 2)
            },
        painter = painterResource(id = resID),
        contentDescription = null
    )
}

@Composable
fun SelectLocation(navAction: NavAction) {
    var isMapLoaded by rememberSaveable{ mutableStateOf(false) }
    val dragDropAnimatable = remember { Animatable(Size.Zero,Size.VectorConverter) }
    val cameraPositionState = rememberCameraPositionState()
    val locationState = rememberMarkerState()
    val viewModel: SelectLocationViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is SelectLocationContract.Effect.ShowToast) {
                showToast(it.msg)
            }
        }.collect()
    }

    // 地图移动，中心的Marker需要动画跳动
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            dragDropAnimatable.animateTo(Size(45F,20F))
        } else {
            dragDropAnimatable.animateTo(Size(25F,11F))
            // 查询附近1000米地址数据
            viewModel.doSearchQueryPoi(cameraPositionState.position.target)
        }
    }

    LaunchedEffect(currentState.isClickForceStartLocation, currentState.currentLocation) {
        val curLocation = currentState.currentLocation
        if(null == curLocation || cameraPositionState.position.target == curLocation) return@LaunchedEffect
        locationState.position = curLocation
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentState.currentLocation, 17F))
    }

    LaunchedEffect(Unit) {
        viewModel.startMapLocation()
        delay(500)
        viewModel.doSearchQueryPoi(cameraPositionState.position.target)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val isDark = isSystemInDarkTheme()
        if (isDark && currentState.mapType == MapType.NORMAL) {
            viewModel.setMapType(MapType.NIGHT)
        }

        GDMap(
            modifier = Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                isZoomGesturesEnabled = true,
                isScrollGesturesEnabled = true,
            ),
            properties = MapProperties(
                mapType = currentState.mapType,
            ),
            onMapLoaded = {
                isMapLoaded = true
            },
        ) {
            Marker(
                state = locationState,
                anchor = Offset(0.5F,0.5F),
                rotation = currentState.currentRotation,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_self),
                onClick = { true }
            )
        }
        if(isMapLoaded) {
            // 地图加载出来之后，再显示出来选点的图标
            UIMarkerInScreenCenter(R.drawable.purple_pin) {
                dragDropAnimatable.value
            }
        }

        Column(
            modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .wrapContentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    RadioButton(
                        modifier = Modifier
                            .size(15.dp),
                        selected = currentState.mapType == MapType.NORMAL || currentState.mapType == MapType.NIGHT,
                        onClick = {
                            viewModel.setMapType(if (isDark) MapType.NIGHT else MapType.NORMAL)
                        }
                    )
                    Text("普通地图", fontSize = 8.sp)

                    RadioButton(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(15.dp),
                        selected = currentState.mapType == MapType.SATELLITE,
                        onClick = {
                            viewModel.setMapType(MapType.SATELLITE)
                        }
                    )
                    Text("卫星地图", fontSize = 8.sp)
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable(
                            onClick = viewModel::startMapLocation,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                ) {
                    Icon(
                        modifier = Modifier.size(60.dp),
                        tint = Color.Unspecified,
                        painter = painterResource(id = R.drawable.ic_map_start_location),
                        contentDescription = null
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(10.dp)
                ) {
                    val latLng = if (currentState.currentSelectLocation != null) {
                        "经纬度：\n${currentState.currentSelectLocation?.latitude},${currentState.currentSelectLocation?.longitude}"
                    } else {
                        "经纬度：\n未知"
                    }
                    val address = if (currentState.currentSelectLocationString.isNotEmpty()) {
                        "当前位置：\n${currentState.currentSelectLocationString}"
                    } else {
                        "当前位置：\n未知"
                    }
                    Text(latLng)
                    Text(address)
                }
            }

            Button(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onClick = {
                    navAction.popBackStack(
                        "pos" to MockLocationContract.Position(
                            lat = currentState.currentSelectLocation?.latitude ?: 0.0,
                            lng = currentState.currentSelectLocation?.longitude ?: 0.0,
                            address = currentState.currentSelectLocationString
                        ),
                    )
                }
            ) {
                Text("选择当前位置")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectLocationPreview() {

}