package cn.fkj233.deviceemulator.xposed.hook.android

import cn.fkj233.deviceemulator.xposed.HookEntry
import cn.fkj233.deviceemulator.xposed.hook.BaseHook

object AndroidHook : BaseHook() {
    override val name: String = "Android Hook"

    val hooks = arrayOf(
        ServiceHook()
    )

    override fun init() {
        HookEntry.initHooks(*hooks)
    }
}