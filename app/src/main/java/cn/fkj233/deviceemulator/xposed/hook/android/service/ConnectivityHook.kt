package cn.fkj233.deviceemulator.xposed.hook.android.service

import android.os.IBinder
import cn.fkj233.deviceemulator.xposed.hook.android.ServiceHook

class ConnectivityHook : ServiceHook() {
    override val name: String = "Connectivity Hook"

    override fun init(serviceName: String, service: IBinder) {

    }
}