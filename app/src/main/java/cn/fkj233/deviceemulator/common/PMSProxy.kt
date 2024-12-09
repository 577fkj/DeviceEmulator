package cn.fkj233.deviceemulator.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.Signature
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import java.lang.reflect.Field


object PMSProxy {
    private const val TAG = "PMSProxy"

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun initAMapKey(packageName: String, fakePackageName: String, amapKey: String, signatureData: String? = null) {
        try {
            if (signatureData != null) {
                val fakeSignature = Signature(Base64.decode(signatureData, Base64.DEFAULT))
                val originalCreator = PackageInfo.CREATOR
                val creator = object : Parcelable.Creator<PackageInfo> {
                    override fun createFromParcel(source: Parcel): PackageInfo {
                        val packageInfo = originalCreator.createFromParcel(source)
                        if (packageInfo.packageName == packageName) {
                            packageInfo.packageName = fakePackageName
                            if (!packageInfo.signatures.isNullOrEmpty()) {
                                packageInfo.signatures!![0] = fakeSignature
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                if (packageInfo.signingInfo != null) {
                                    val signaturesArray = packageInfo.signingInfo?.apkContentsSigners
                                    if (!signaturesArray.isNullOrEmpty()) {
                                        signaturesArray[0] = fakeSignature
                                    }
                                }
                            }
                        }
                        return packageInfo
                    }

                    override fun newArray(size: Int): Array<out PackageInfo>? {
                        return originalCreator.newArray(size)
                    }
                }
                try {
                    findField(PackageInfo::class.java, "CREATOR").set(null, creator)
                    Log.d(TAG, "PackageInfo: set creator success")
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }

            val originalCreator2 = ApplicationInfo.CREATOR
            val creator2 = object : Parcelable.Creator<ApplicationInfo> {
                override fun createFromParcel(source: Parcel): ApplicationInfo {
                    val applicationInfo = originalCreator2.createFromParcel(source)
                    Log.d(TAG, "createFromParcel: ${applicationInfo.packageName}")
                    Log.d(TAG, "packageName: $packageName")
                    Log.d(TAG, "applicationInfo.packageName == packageName: ${applicationInfo.packageName == packageName}")
                    Log.d(TAG, "applicationInfo.metaData: ${applicationInfo.metaData}")
                    if (applicationInfo.packageName == packageName && applicationInfo.metaData != null) {
                        applicationInfo.packageName = fakePackageName

                        Log.d(TAG, "applicationInfo.metaData: ${applicationInfo.metaData.deepCopy()}")

                        val metaData = Bundle()

                        Log.d(TAG, "applicationInfo.metaData1: ${applicationInfo.metaData}")

                        metaData.putString("com.amap.api.v2.apikey", amapKey)

                        applicationInfo.metaData = metaData
                        Log.d(TAG, "applicationInfo.metaData2: ${applicationInfo.metaData}")
                    }
                    return applicationInfo
                }

                override fun newArray(size: Int): Array<out ApplicationInfo>? {
                    return originalCreator2.newArray(size)
                }
            }

            try {
                findField(ApplicationInfo::class.java, "CREATOR").set(null, creator2)
                Log.d(TAG, "ApplicationInfo: set creator success")
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

            try {
                val mCreators = findField(Parcel::class.java, "mCreators").get(null) as HashMap<*, *>
                val sPairedCreators = findField(Parcel::class.java, "sPairedCreators").get(null) as HashMap<*, *>

                //清除调用条件
                mCreators.clear()
                sPairedCreators.clear()
            } catch (ignored: Exception) {
                Log.e(TAG, "initAMapKey: ", ignored)
            }
            Log.d(TAG, "initAMapKey: success")
        } catch (e: Exception) {
            Log.e(TAG, "initAMapKey: ", e)
        }
    }

    private fun findField(clazz: Class<*>, name: String): Field {
        try {
            val field = clazz.getDeclaredField(name)
            field.isAccessible = true
            return field
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        }
    }
}