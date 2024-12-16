package cn.fkj233.deviceemulator.app

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.app.ui.common.utils.SDKUtils
import cn.fkj233.deviceemulator.common.PMSProxy

class MainApplication: Application() {
//    init {
//        val fakePackageName = BuildConfig.AMAP_FAKE_PACKAGE_NAME ?: BuildConfig.APPLICATION_ID
//        PMSProxy.initAMapKey(BuildConfig.APPLICATION_ID, fakePackageName, BuildConfig.AMAP_KEY, BuildConfig.AMAP_SIGNATURE)
//        Log.d("MainApplication", "AMap key initialized")
//    }

    override fun onCreate() {
        super.onCreate()
        SDKUtils.init(this)
    }
}