package cn.fkj233.deviceemulator.xposed.hook.android

import android.annotation.SuppressLint
import android.location.GnssStatusHidden
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.location.LocationRequestHidden
import android.os.Build
import android.os.IBinder
import cn.fkj233.deviceemulator.xposed.ServiceHelper
import cn.fkj233.deviceemulator.xposed.mock.gps.GPSMock
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethodOrNull
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import dev.rikka.tools.refine.Refine
import java.lang.reflect.Proxy

class LocationHook : ServiceHook() {
    override val name: String = "Location Hook"

    @SuppressLint("InlinedApi")
    val allowProvider = arrayOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
        LocationManager.FUSED_PROVIDER
    )

    private val mLocationChangedListenerMap: MutableMap<Any, String> = mutableMapOf()
    private val mLocationChangedListenerProxyMap: MutableMap<Any, Any> = mutableMapOf()
    private val mLocationChangedListenerProviderMap: MutableMap<Any, String> = mutableMapOf()

    fun addListener(packageName: String, listener: Any, proxy: Any?, provider: String?) {
        synchronized(mLocationChangedListenerMap) {
            mLocationChangedListenerMap[listener] = packageName
        }
        if (proxy != null) {
            synchronized(mLocationChangedListenerProxyMap) {
                mLocationChangedListenerProxyMap[listener] = proxy
            }
        }
        if (provider != null) {
            synchronized(mLocationChangedListenerMap) {
                mLocationChangedListenerProviderMap[listener] = provider
            }
        }
    }

    fun removeListener(listener: Any) {
        synchronized(mLocationChangedListenerMap) {
            mLocationChangedListenerMap.remove(listener)
        }
        synchronized(mLocationChangedListenerProviderMap) {
            mLocationChangedListenerProviderMap.remove(listener)
        }
    }

    fun getProxyListener(listener: Any): Any? {
        return mLocationChangedListenerProxyMap[listener]
    }

    fun removeProxyListener(listener: Any) {
        synchronized(mLocationChangedListenerProxyMap) {
            mLocationChangedListenerProxyMap.remove(listener)
        }
    }

    fun getLocationRequestProvider(locationRequest: Any): String {
        return Refine.unsafeCast<LocationRequestHidden>(locationRequest).provider
    }

    // SDK 小于 31
    fun <T> Class<T>.hookSDK30() {

    }

    // SDK 大于 31
    @SuppressLint("NewApi", "PrivateApi")
    fun <T> Class<T>.hookSDK31() {
        findMethodOrNull {
            name == "registerLocationListener"
        }?.apply {
            hookBefore {
                val locationRequest = it.args[1] as LocationRequest
                val listener = it.args[2]
                val packageName = it.args[3] as String
                addListener(packageName, listener, null, getLocationRequestProvider(locationRequest))
            }
//            hookAfter {
//                synchronized(mock)
//            }
        }
        findMethodOrNull {
            name == "unregisterLocationListener"
        }?.hookBefore {
            runCatching {
                val listener = it.args[0]
                if (ServiceHelper.isHook()) {
                    listener.invokeMethod("onLocationChanged", args(listOf(Location(GPSMock.getMockGPSLocation())), null))
                }

                removeListener(listener)

                val proxy = getProxyListener(listener)
                if (proxy != null) {
                    removeProxyListener(proxy)
                }
            }.onFailure {
                Log.ex(it)
            }
        }

        findMethodOrNull {
            name == "registerGnssStatusCallback"
        }?.hookBefore {
            if (ServiceHelper.isSystem()) {
                return@hookBefore
            }
            val thisObject = it.thisObject
            val listener = it.args[0]
            val packageName = it.args[1] as String

            val classLoader = thisObject.javaClass.classLoader
            val newProxyInstance = Proxy.newProxyInstance(classLoader, arrayOf(classLoader!!.loadClass("android.location.IGnssStatusListener"))) { proxy, method, args ->
                when (method.name) {
                    "onSvStatusChanged" -> {
                        GnssStatusHidden.wrap()
                    }
                }

            }
            addIGnssStatusListener(str, obj2, newProxyInstance)
        }
    }

    override fun init(serviceName: String, service: IBinder) {
        serviceClass.apply {
            findMethodOrNull {
                name == "isProviderEnabledForUser"
            }?.hookBefore {
                if (!ServiceHelper.isHook()) {
                    return@hookBefore
                }

                val provider = it.args[0] as String
                if (provider in allowProvider) {
                    it.result = true
                }
            }

            findMethodOrNull {
                name == "getBestProvider"
            }?.hookBefore {
                if (!ServiceHelper.isHook()) {
                    return@hookBefore
                }

                it.result = LocationManager.GPS_PROVIDER
            }

            findMethodOrNull {
                name == "getProviders"
            }?.hookBefore {
                if (!ServiceHelper.isHook()) {
                    return@hookBefore
                }

                it.result = arrayListOf(LocationManager.GPS_PROVIDER)
            }

            findAllMethods {
                name == "getLastLocation"
            }.hookBefore {
                if (!ServiceHelper.isHook()) {
                    return@hookBefore
                }
                val location = Location(GPSMock.getMockGPSLocation())
                location.provider = getLocationRequestProvider(it.args[0])
                it.result = location
            }

            if (Build.VERSION.SDK_INT < 31) {
                hookSDK30()
            } else {
                hookSDK31()
            }
        }


    }
}