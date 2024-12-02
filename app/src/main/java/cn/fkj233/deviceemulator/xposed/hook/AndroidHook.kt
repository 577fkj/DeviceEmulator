package cn.fkj233.deviceemulator.xposed.hook

import android.annotation.SuppressLint
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.service.DeviceEmulatorService
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import com.kaisar.xservicemanager.XServiceManager

object AndroidHook : BaseHook() {
    override val name: String = "Android Hook"
    @SuppressLint("StaticFieldLeak")
    private var mService : DeviceEmulatorService? = null

    override fun init() {
        XServiceManager.initForSystemServer()
        XServiceManager.addPackage(BuildConfig.APPLICATION_ID)
        XServiceManager.registerService(DeviceEmulatorManager.SERVICE_NAME) {
            mService = DeviceEmulatorService(it)
            mService
        }
    }
}