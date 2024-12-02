package cn.fkj233.deviceemulator.xposed.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class BaseHook {
    abstract fun init(lpparam: XC_LoadPackage.LoadPackageParam)
    abstract val name: String
    var isInit: Boolean = false
}