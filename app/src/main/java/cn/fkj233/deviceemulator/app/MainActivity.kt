package cn.fkj233.deviceemulator.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.app.ui.theme.MyApplicationTheme
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import cn.fkj233.xservicemanager.XServiceManager
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.position.rememberCameraPositionState


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"

        var service: IDeviceEmulatorInterface? = null
    }

    private val viewModel: DeviceEmulatorHomeModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        MapsInitializer.setTerrainEnable(true)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DeviceEmulatorApp(calculateWindowSizeClass(this))
//
//                PermissionScreen()
//
//
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    val cameraPositionState = rememberCameraPositionState {
//                        position = CameraPosition.fromLatLngZoom(LatLng(39.984108,116.307557), 15F)
//                    }
//                    GDMap(
//                        modifier = Modifier.fillMaxSize(),
//                        cameraPositionState = cameraPositionState,
//                        uiSettings = MapUiSettings(
//                            isZoomGesturesEnabled = true,
//                            isScrollGesturesEnabled = true,
//                            isRotateGesturesEnabled = true,
//                            isZoomEnabled = true,
//                            isTiltGesturesEnabled = true,
//                            isCompassEnabled = true,
//                            isScaleControlsEnabled = true,
//                        )
//                    ){
//                        //这里面放地图覆盖物...
//                    }
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
            }
        }

        service = XServiceManager.getServiceInterface(DeviceEmulatorManager.SERVICE_NAME)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}