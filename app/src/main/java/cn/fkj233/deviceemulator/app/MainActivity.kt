package cn.fkj233.deviceemulator.app

import android.annotation.SuppressLint
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
        var service: IDeviceEmulatorInterface? = null
        @SuppressLint("StaticFieldLeak")
        var manager: DeviceEmulatorManager? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        MapsInitializer.setTerrainEnable(true)

        service = XServiceManager.getServiceInterface(DeviceEmulatorManager.SERVICE_NAME)
        manager = service?.let { DeviceEmulatorManager(it) }

        enableEdgeToEdge()
        setContent {
            DeviceEmulatorApplicationTheme {
                DeviceEmulatorApp()
            }
        }
    }
}