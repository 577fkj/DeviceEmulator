package cn.fkj233.deviceemulator.xposed.hook.android

import android.os.IBinder

class ConnectivityHook : ServiceHook() {
    override val name: String = "Connectivity Hook"

    override fun init(serviceName: String, service: IBinder) {

    }
}