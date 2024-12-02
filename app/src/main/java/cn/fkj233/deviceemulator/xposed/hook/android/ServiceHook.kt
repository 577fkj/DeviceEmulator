package cn.fkj233.deviceemulator.xposed.hook.android

import android.os.IBinder
import com.github.kyuubiran.ezxhelper.utils.findField

abstract class ServiceHook {
    abstract fun init(serviceName: String, serviceClass: Class<*>, service: IBinder)
    abstract val name: String
    var isInit: Boolean = false

    fun getTransactId(serviceClass: Class<*>, name: String): Int {
        val field = serviceClass.findField(true) {
            name == "TRANSACTION_$name"
        }
        return field.getInt(null)
    }
}