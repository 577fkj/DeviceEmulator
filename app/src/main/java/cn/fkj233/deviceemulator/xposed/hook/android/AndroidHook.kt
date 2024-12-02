package cn.fkj233.deviceemulator.xposed.hook.android

import android.content.Context
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.service.DeviceEmulatorService
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import cn.fkj233.deviceemulator.xposed.hook.BaseHook
import cn.fkj233.xservicemanager.XServiceManager
import com.github.kyuubiran.ezxhelper.utils.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage

object AndroidHook : BaseHook() {
    override val name: String = "Android Hook"

    private var mService : DeviceEmulatorService? = null

    private val serviceHooks = mapOf(
        Context.WIFI_SERVICE to WifiHook()
    )

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        XServiceManager.initForSystemServer(true)
        XServiceManager.setWhiteList(true)
        XServiceManager.addPackage(BuildConfig.APPLICATION_ID)
        XServiceManager.registerService(DeviceEmulatorManager.SERVICE_NAME, true) {
            mService = DeviceEmulatorService(it)
            mService!!
        }
        XServiceManager.setAddServiceCallback { sName, service ->
            Log.dx("AddService $sName $service")
            serviceHooks.getOrDefault(sName, null)?.apply {
                val serviceClassName = service.javaClass
                init(sName, serviceClassName, service)
                isInit = true
                Log.dx("Init service hook $name")
            }
        }
    }
}