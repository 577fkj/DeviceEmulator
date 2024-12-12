package cn.fkj233.deviceemulator.xposed.hook.android

import android.os.IBinder

class LocationHook : ServiceHook() {
    override val name: String = "Location Hook"

    override fun init(serviceName: String, service: IBinder) {

    }
}