package cn.fkj233.deviceemulator.xposed.hook

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReplace
import de.robv.android.xposed.callbacks.XC_LoadPackage

object SelfHook : BaseHook() {
    override val name: String = "Self Hook"

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        findMethod("cn.fkj233.deviceemulator.app.ui.common.utils.XposedData") {
            name == "isActive"
        }.hookReplace {
            true
        }
    }
}