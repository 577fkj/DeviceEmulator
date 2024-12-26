package cn.fkj233.deviceemulator.xposed.hook.android.service

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.location.LocationRequestHidden
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import cn.fkj233.deviceemulator.xposed.ServiceHelper
import cn.fkj233.deviceemulator.xposed.hook.android.ServiceHook
import cn.fkj233.deviceemulator.xposed.hook.android.hookMethodAfter
import cn.fkj233.deviceemulator.xposed.hook.android.hookMethodBefore
import cn.fkj233.deviceemulator.xposed.mock.gps.GPSMock
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.args
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.loadClass
import de.robv.android.xposed.XC_MethodHook
import dev.rikka.tools.refine.Refine
import java.util.Timer
import java.util.TimerTask

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

    private fun addListener(packageName: String, listener: Any, proxy: Any?, provider: String?) {
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

    private fun removeListener(listener: Any) {
        synchronized(mLocationChangedListenerMap) {
            mLocationChangedListenerMap.remove(listener)
        }
        synchronized(mLocationChangedListenerProviderMap) {
            mLocationChangedListenerProviderMap.remove(listener)
        }
    }

    private fun getProxyListener(listener: Any): Any? {
        return mLocationChangedListenerProxyMap[listener]
    }

    private fun removeProxyListener(listener: Any) {
        synchronized(mLocationChangedListenerProxyMap) {
            mLocationChangedListenerProxyMap.remove(listener)
        }
    }

    private fun getLocationRequestProvider(locationRequest: Any): String {
        return Refine.unsafeCast<LocationRequestHidden>(locationRequest).provider
    }

    // SDK 小于 31
    private fun <T> Class<T>.hookSDK30() {

    }

    // SDK 大于 31
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("NewApi", "PrivateApi")
    fun <T> Class<T>.hookSDK31() {
        hookMethodAfter("registerLocationListener") {
            val locationRequest = it.args[1] as LocationRequest
            val listener = it.args[2]
            val packageName = it.args[3] as String
            addListener(packageName, listener, null, getLocationRequestProvider(locationRequest))

            val uid = Binder.getCallingUid()
            ServiceHelper.notifyRequestLocation(packageName, uid, ServiceHelper.isSystem(uid))

            Log.ix("registerLocationListener: $locationRequest, $listener, $packageName")
        }

        hookMethodBefore("unregisterLocationListener") {
            val listener = it.args[0]
            if (ServiceHelper.isHook()) {
                listener.invokeMethod("onLocationChanged", args(listOf(Location(GPSMock.getMockGPSLocation())), argTypes(List::class.java)))
            }

            removeListener(listener)

            val proxy = getProxyListener(listener)
            if (proxy != null) {
                removeProxyListener(proxy)
            }

            Log.ix("unregisterLocationListener: $listener")
        }

        // Gnss Status
//        findMethodOrNull {
//            name == "registerGnssStatusCallback"
//        }?.hookBefore {
//            if (ServiceHelper.isSystem()) {
//                return@hookBefore
//            }
//            val thisObject = it.thisObject
//            val listener = it.args[0]
//            val packageName = it.args[1] as String
//
//            val classLoader = thisObject.javaClass.classLoader
//            val newProxyInstance = Proxy.newProxyInstance(classLoader, arrayOf(classLoader!!.loadClass("android.location.IGnssStatusListener"))) { proxy, method, args ->
//                when (method.name) {
//                    "onSvStatusChanged" -> {
////                        GnssStatusHidden.wrap()
//                    }
//                }
//
//            }
////            addIGnssStatusListener(str, obj2, newProxyInstance)
//        }
    }

    fun isProvider(param: XC_MethodHook.MethodHookParam) {
        if (!ServiceHelper.isHook()) {
            return
        }

        val provider = param.args[0] as String
        if (provider in allowProvider) {
            param.result = true
        }
    }

    fun getProviderByListener(listener: Any): String? {
        return mLocationChangedListenerProviderMap[listener]
    }

    fun onLocationChanged(param: XC_MethodHook.MethodHookParam) {
        @Suppress("UNCHECKED_CAST")
        val locationList = param.args[0] as List<Location>?
        val listener = param.thisObject

        Log.ix("onLocationChange: $locationList")

        if (locationList != null) {
            if (locationList.isNotEmpty() && ServiceHelper.isMocking()) {
                val location = Location(GPSMock.getMockGPSLocation())

                val provider = getProviderByListener(listener)
                if (provider != null) {
                    location.provider = provider
                } else {
                    location.provider = locationList[0].provider
                }

                val bundle = locationList[0].extras
                if (bundle != null) {
                    location.extras = Bundle(bundle)
                }

                Log.ix("onLocationChange: Change provider $provider location $location")

                param.args[0] = listOf(location)
            }
        }

        Log.ix("onLocationChange: ${param.args[0]}")
    }

    private lateinit var mIRemoteCallbackClazz: Class<*>

    fun callLocationChanged(location: Location) {
        Log.ix("callLocationChanged: $location")
        runCatching {
            synchronized(mLocationChangedListenerMap) {
                mLocationChangedListenerMap.forEach { (listener, _) ->
                    listener.invokeMethod("onLocationChanged", args(location, null), argTypes(Location::class.java, mIRemoteCallbackClazz))
                }
            }
        }.onFailure {
            Log.ex("callLocationChanged", it)
        }
    }

    override fun init(serviceName: String, service: IBinder) {
        serviceClass.apply {
            hookMethodBefore("isProviderEnabled", this@LocationHook::isProvider)
            hookMethodBefore("isProviderEnabledForUser", this@LocationHook::isProvider)

            hookMethodBefore("getBestProvider") {
                if (!ServiceHelper.isHook()) {
                    return@hookMethodBefore
                }

                it.result = LocationManager.GPS_PROVIDER
            }

            hookMethodBefore("getProviders") {
                if (!ServiceHelper.isHook()) {
                    return@hookMethodBefore
                }

                it.result = arrayListOf(LocationManager.GPS_PROVIDER)
            }

            hookMethodBefore("getLastLocation") {
                if (!ServiceHelper.isHook()) {
                    return@hookMethodBefore
                }

                val location = Location(GPSMock.getMockGPSLocation())
                location.provider = getLocationRequestProvider(it.args[0])
                it.result = location
            }

            if (Build.VERSION.SDK_INT < 31) {
                hookSDK30()
            } else {
                hookSDK31()
                loadClass("android.location.ILocationListener\$Stub\$Proxy").hookMethodBefore("onLocationChanged", this@LocationHook::onLocationChanged)
                loadClass("android.location.LocationManager\$LocationListenerTransport").hookMethodBefore("onLocationChanged", this@LocationHook::onLocationChanged)

                mIRemoteCallbackClazz = classLoader.loadClass("android.os.IRemoteCallback")
            }
        }

        val locationService = ServiceHelper.getMockLocationService()

        var timer = Timer()
        locationService.addOnMockStatusChangedListener {
            Log.ix("Mock Status Changed: $it")
            if (it) {
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        callLocationChanged(Location(GPSMock.getMockGPSLocation()))
                    }
                }, 0, locationService.locationUpdateInterval)
            } else {
                timer.cancel()
            }
        }
    }
}