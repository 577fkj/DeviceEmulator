package cn.fkj233.deviceemulator.xposed.hook.android

import android.os.IBinder
import android.os.Parcel
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.findFieldOrNull
import com.github.kyuubiran.ezxhelper.utils.findMethodOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore

typealias ServiceHookCallback = (Int, Parcel, Parcel?) -> Boolean

abstract class ServiceHook {
    abstract fun init(serviceName: String, service: IBinder)
    abstract val name: String
    var isInit: Boolean = false
    var descriptor = ""
    private lateinit var serviceClass: Class<*>

    private val transactMapBefore = hashMapOf<Int, ServiceHookCallback>()
    private val transactMapAfter = hashMapOf<Int, ServiceHookCallback>()


    fun init(serviceName: String, clazz: Class<*>, service: IBinder) {
        serviceClass = clazz
        serviceClass.initHookTransact()
        init(serviceName, service)
    }

    private fun Class<*>.initHookTransact() {
        val method = this.findMethodOrNull(true) {
            name == "onTransact"
        }
        if (method == null){
            Log.ix("$name: onTransact not found")
            return
        }
        Log.ix("$name: Hooked onTransact $method")
        descriptor = getFieldValue(this, "DESCRIPTOR") ?: ""
        if (descriptor == "") {
            Log.ix("$name: Descriptor not found")
            return
        }
        Log.ix("$name: Get descriptor $descriptor")
        method.apply {
            hookBefore {
                val code = it.args[0] as Int
                val data = it.args[1] as Parcel
                val reply = it.args[2] as Parcel?
                val cb = transactMapBefore.getOrDefault(code, null)
                if (cb != null) {
                    Log.ix("$name: Hook before $code")
                    data.enforceInterface(descriptor)
                    if (cb(code, data, reply)) {
                        it.result = true
                    }
                }
            }
            hookAfter {
                val code = it.args[0] as Int
                val data = it.args[1] as Parcel
                val reply = it.args[2] as Parcel?
                val cb = transactMapAfter.getOrDefault(code, null)
                if (cb != null) {
                    if (cb(code, data, reply)) {
                        it.result = true
                    }
                }
            }
        }
    }

    fun addTransactHookBefore(name: String, cb: ServiceHookCallback) {
        val id = getTransactId(serviceClass, name)
        if (id == -1) {
            Log.ix("$name: Transaction $name not found")
            return
        }
        Log.ix("$name: Add hook before $name $id")
        transactMapBefore[id] = cb
    }

    fun addTransactHookAfter(name: String, cb: ServiceHookCallback) {
        val id = getTransactId(serviceClass, name)
        if (id == -1) {
            Log.ix("$name: Transaction $name not found")
            return
        }
        Log.ix("$name: Add hook after $name $id")
        transactMapBefore[id] = cb
    }

    fun getTransactId(serviceClass: Class<*>, name: String): Int {
        return getFieldValue(serviceClass, "TRANSACTION_$name") ?: -1
    }

    fun <T> getFieldValue(serviceClass: Class<*>, n: String): T? {
        val field = serviceClass.findFieldOrNull(true) {
            n == name
        }
        if (field == null) {
            Log.ix("$name: Field $n not found")
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return field.get(null) as T
    }
}