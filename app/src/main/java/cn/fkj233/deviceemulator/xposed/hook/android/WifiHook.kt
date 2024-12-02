package cn.fkj233.deviceemulator.xposed.hook.android

import android.os.IBinder
import android.os.Parcel
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter

class WifiHook : ServiceHook() {
    override val name: String = "Wifi Hook"

    private var getConnectionInfoId: Int = -1

    override fun init(serviceName: String, serviceClass: Class<*>, service: IBinder) {
        Log.dx("WifiHook init")
        Log.dx("ServiceName: $serviceName")
        Log.dx("ServiceClass: $serviceClass")
        Log.dx("ServiceClass Superclass: ${serviceClass.superclass}")
        Log.dx("ServiceClass IInterface: ${serviceClass.interfaces}")
        Log.dx("Service: $service")
//        getConnectionInfoId = getTransactId(serviceClass, "getConnectionInfo")
//        serviceClass.findMethod {
//            name == "onTransact"
//        }.hookAfter {
//            val code = it.args[0] as Int
//            val data = it.args[1] as Parcel
//            val reply = it.args[2] as Parcel?
//            when (code) {
//                getConnectionInfoId -> {
//
//                }
//            }
//        }
    }
}