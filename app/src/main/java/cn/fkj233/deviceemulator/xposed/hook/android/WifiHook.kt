package cn.fkj233.deviceemulator.xposed.hook.android

import android.R.attr.data
import android.annotation.SuppressLint
import android.net.wifi.WifiInfo
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore


class WifiHook : ServiceHook() {
    override val name: String = "Wifi Hook"

    private var getConnectionInfoId: Int = -1
    private var DESCRIPTOR = ""

    @SuppressLint("NewApi")
    override fun init(serviceName: String, serviceClass: Class<*>, service: IBinder) {
        Log.ix("WifiHook init")
//        Log.dx("ServiceName: $serviceName")
//        Log.dx("ServiceClass: $serviceClass")
//        Log.dx("ServiceClass Superclass: ${serviceClass.superclass}")
//        Log.dx("ServiceClass IInterface: ${serviceClass.interfaces}")
//        Log.dx("Service: $service")
        val wifi = WifiInfo.Builder()
            .setRssi(-10)
            .setSsid("TestWifi".toByteArray())
            .setBssid("00:00:00:00:00:00")
            .setNetworkId(0)
            .build()
        getConnectionInfoId = getTransactId(serviceClass, "getConnectionInfo")
        DESCRIPTOR = getFieldValue(serviceClass, "DESCRIPTOR")
        serviceClass.findMethod(true) {
            name == "onTransact"
        }.apply {
            Log.ix("Hooked onTransact $this")
            hookBefore {
                val code = it.args[0] as Int
                val data = it.args[1] as Parcel
                val reply = it.args[2] as Parcel?
                when (code) {
                    getConnectionInfoId -> {
                        Log.ix("DESCRIPTOR: $DESCRIPTOR")
                        data.enforceInterface(DESCRIPTOR)
                        Log.ix("Hooked getConnectionInfo")
                        val callingPackage = data.readString()
                        Log.ix("CallPackageName: $callingPackage")
                        val callingFeatureId = data.readString()
                        Log.ix("CallFeatureId: $callingFeatureId")
                        reply?.writeNoException()
                        reply?.writeTypedObject(wifi, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                        it.result = true
                    }
                }
            }
        }
    }
}