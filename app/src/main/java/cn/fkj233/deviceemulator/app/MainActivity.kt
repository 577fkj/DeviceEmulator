package cn.fkj233.deviceemulator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.app.ui.theme.DeviceEmulatorApplicationTheme
import com.amap.api.maps.MapsInitializer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        MapsInitializer.setTerrainEnable(true)

        enableEdgeToEdge()
        setContent {
            DeviceEmulatorApplicationTheme {
                DeviceEmulatorApp()
            }
        }
    }
}