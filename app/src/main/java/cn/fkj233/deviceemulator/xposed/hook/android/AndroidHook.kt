package cn.fkj233.deviceemulator.xposed.hook.android

import android.content.Context
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.app.manager.DeviceEmulatorManager
import cn.fkj233.deviceemulator.xposed.ServiceHelper
import cn.fkj233.deviceemulator.xposed.hook.BaseHook
import cn.fkj233.deviceemulator.xposed.hook.android.service.LocationHook
import cn.fkj233.deviceemulator.xposed.hook.android.service.WifiHook
import cn.fkj233.xservicemanager.XServiceManager
import com.github.kyuubiran.ezxhelper.utils.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage

object AndroidHook : BaseHook() {
    override val name: String = "Android Hook"

    private val serviceHooks = mapOf(
        Context.WIFI_SERVICE to WifiHook(),
        Context.LOCATION_SERVICE to LocationHook()
//        Context.CONNECTIVITY_SERVICE to ConnectivityHook()
    )

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        XServiceManager.initForSystemServer(true)
        XServiceManager.setWhiteList(true)
        XServiceManager.addPackage(BuildConfig.APPLICATION_ID)
        XServiceManager.addService(DeviceEmulatorManager.SERVICE_NAME, ServiceHelper.getDeviceEmulatorService(), true)
        XServiceManager.setAddServiceCallback { sName, service ->
//            Log.dx("AddService $sName $service")
            serviceHooks.getOrDefault(sName, null)?.apply {
                runCatching {
                    val serviceClassName = service.javaClass
                    init(sName, serviceClassName, service)
                    isInit = true
                    Log.ix("Init service hook $name")
                }.onFailure {
                    Log.ex(it)
                }
            }
        }
    }
}