package cn.fkj233.deviceemulator.xposed.hook.android

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.os.Parcel
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.common.CallingHelper
import cn.fkj233.deviceemulator.common.Constants
import cn.fkj233.deviceemulator.service.DeviceEmulatorService
import cn.fkj233.deviceemulator.xposed.hook.BaseHook
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore


class ServiceHook : BaseHook() {
    override val name: String = "Service Hook"

    private var mService : DeviceEmulatorService? = null
    private var callingHelper : CallingHelper? = null
    private var context: Context? = null

    fun getSystemContext(): Context {
        if (context != null) {
            return context!!
        }
        @SuppressLint("PrivateApi") val activityThreadClass =
            Class.forName("android.app.ActivityThread")
        val currentActivityThread = activityThreadClass.getMethod("currentActivityThread")
        val getSystemContext = activityThreadClass.getMethod("getSystemContext")
        val systemContext =
            getSystemContext.invoke(currentActivityThread.invoke(null)) as Context
        context = systemContext
        return systemContext
    }

    override fun init() {
        findMethod("android.os.ServiceManager") {
            name == "addService"
        }.hookBefore { addService ->
            val sName = addService.args[0] as String
            val service = addService.args[1] as IBinder
            if (sName == "clipboard") {
                mService = DeviceEmulatorService(getSystemContext())
                callingHelper = CallingHelper(getSystemContext())

                service.javaClass.findMethod {
                    name == "onTransact"
                }.hookBefore {
                    val code = it.args[0] as Int
                    val data = it.args[1] as Parcel
                    val reply = it.args[2] as Parcel?
                    if (myTransact(code, data, reply)) {
                        it.result = true
                    }
                }
            }
        }
    }

    private fun myTransact(code: Int, data: Parcel, reply: Parcel?): Boolean {
        if (code == Constants.TRANSACTION) {
            if (callingHelper!!.callingPackageName != BuildConfig.APPLICATION_ID) {
                Log.e("Calling package name is not correct")
                return false
            }
            runCatching {
                data.enforceInterface(Constants.DESCRIPTOR)
                when (data.readInt()) {
                    Constants.ACTION_GET_BINDER -> {
                        reply?.writeNoException()
                        reply?.writeStrongBinder(mService?.asBinder())
                        return true
                    }

                    else -> Log.w("Unknown action")
                }
            }.onFailure {
                Log.e("Transaction error", it)
            }
            data.setDataPosition(0)
            reply?.setDataPosition(0)
        }
        return false
    }
}