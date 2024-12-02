package cn.fkj233.deviceemulator.xposed

import cn.fkj233.deviceemulator.xposed.hook.android.AndroidHook
import cn.fkj233.deviceemulator.xposed.hook.BaseHook
import cn.fkj233.deviceemulator.xposed.hook.PhoneHook
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val ANDROID_PACKAGE_NAME = "android"
private const val PHONE_PACKAGE_NAME = "com.android.phone"
private const val TAG = "DeviceEmulator"

class HookEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        fun initHooks(vararg hook: BaseHook) {
            hook.forEach {
                runCatching {
                    if (it.isInit) return@forEach
                    it.init()
                    it.isInit = true
                    Log.i("Inited hook: ${it.name}")
                }.logexIfThrow("Failed init hook: ${it.name}")
            }
        }
    }

    private val hookMap = mapOf(
        ANDROID_PACKAGE_NAME to AndroidHook,
        PHONE_PACKAGE_NAME to PhoneHook
    )

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (hookMap.keys.contains(lpparam.packageName)) {
            EzXHelperInit.initHandleLoadPackage(lpparam)
            EzXHelperInit.setLogTag(TAG)
            EzXHelperInit.setToastTag(TAG)
            initHooks(hookMap[lpparam.packageName]!!)
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }
}