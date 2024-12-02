package cn.fkj233.deviceemulator.xposed.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage

object PhoneHook : BaseHook() {
    override val name: String = "Phone Hook"

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {

    }

}