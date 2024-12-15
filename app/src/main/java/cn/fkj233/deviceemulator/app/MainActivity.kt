package cn.fkj233.deviceemulator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.app.ui.theme.DeviceEmulatorApplicationTheme
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import cn.fkj233.xservicemanager.XServiceManager
import com.amap.api.maps.MapsInitializer


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"

        var service: IDeviceEmulatorInterface? = null
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        MapsInitializer.setTerrainEnable(true)

        service = XServiceManager.getServiceInterface(DeviceEmulatorManager.SERVICE_NAME)

        enableEdgeToEdge()
        setContent {
            DeviceEmulatorApplicationTheme {
                DeviceEmulatorApp()
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
    DeviceEmulatorApplicationTheme {
        Greeting("Android")
    }
}