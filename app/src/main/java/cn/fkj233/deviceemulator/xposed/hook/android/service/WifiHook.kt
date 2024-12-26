package cn.fkj233.deviceemulator.xposed.hook.android.service

import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiInfoHidden
import android.net.wifi.WifiSsid
import android.os.IBinder
import android.os.Parcelable
import cn.fkj233.deviceemulator.common.InetAddressHelper
import cn.fkj233.deviceemulator.xposed.ServiceHelper
import cn.fkj233.deviceemulator.xposed.hook.android.ServiceHook
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethodOrNull
import com.github.kyuubiran.ezxhelper.utils.invokeAs
import com.github.kyuubiran.ezxhelper.utils.newInstanceAs
import dev.rikka.tools.refine.Refine


class WifiHook : ServiceHook() {
    override val name: String = "Wifi Hook"

    private fun createWifiSsid(ssid: String): WifiSsid? {
        val wifiSsidClass = Class.forName("android.net.wifi.WifiSsid")

        var wifiSsid = wifiSsidClass.findMethodOrNull {
            name == "fromBytes"
        }?.invokeAs<WifiSsid>(null, ssid.toByteArray())

        if (wifiSsid == null) {
            wifiSsid = wifiSsidClass.findMethodOrNull {
                name == "createFromByteArray"
            }?.invokeAs<WifiSsid>(null, ssid)
        }

        if (wifiSsid == null) {
            val hex = ssid.toByteArray().joinToString("") {
                it.toString(16).padStart(2, '0')
            }
            wifiSsid = wifiSsidClass.findMethodOrNull {
                name == "createFromHex"
            }?.invokeAs<WifiSsid>(null, hex)
        }

        return wifiSsid
    }

    private fun getEmptyWifiInfo(): WifiInfo? = WifiInfo::class.java.newInstanceAs()
    private fun getWifiInfo(): WifiInfo? {
        val wifiInfo = getEmptyWifiInfo() ?: return null
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setSSID(createWifiSsid("TTTTT"))
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setMacAddress("1E:40:E8:10:ED:2B")
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setRssi(-10)
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setBSSID("56:16:51:7C:69:C7")
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setLinkSpeed(1000)
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setFrequency(2437)
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setNetworkId(1000)
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .score = 60
        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setSupplicantState(SupplicantState.COMPLETED)

        val ipAddress = InetAddressHelper.getIpv4InetAddress("114.114.114.114")

        Refine.unsafeCast<WifiInfoHidden>(wifiInfo)
            .setInetAddress(ipAddress)

        return wifiInfo
    }

    override fun init(serviceName: String, service: IBinder) {
        Log.ix("WifiHook init")
        val wifi = getWifiInfo()
        addTransactHookBefore("getConnectionInfo") { code, data, reply ->
            if (!ServiceHelper.isHook()) {
                return@addTransactHookBefore false
            }
            Log.ix("Hooked getConnectionInfo")
            val callingPackage = data.readString()
            Log.ix("CallPackageName: $callingPackage")
            val callingFeatureId = data.readString()
            Log.ix("CallFeatureId: $callingFeatureId")
            reply?.writeNoException()
            reply?.writeTypedObject(wifi, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
            true
        }
    }
}