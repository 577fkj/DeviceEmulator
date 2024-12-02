package cn.fkj233.deviceemulator.common

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Binder
import com.github.kyuubiran.ezxhelper.utils.Log


class CallingHelper(private val context: Context) {
    private var activityManager: ActivityManager? = null
    private var lastCheckTime: Long = 0
    private var lastProcessInfo: List<RunningAppProcessInfo>? = null

    val callingPackageName: String
        get() = getCallingPackageName(Binder.getCallingUid(), Binder.getCallingPid())

    fun getCallingPackageName(uid: Int, pid: Int): String {
        if (uid != -1) {
            var packages: Array<String?>? = null
            try {
                packages = context.packageManager.getPackagesForUid(uid)
            } catch (e: Exception) {
                Log.e("Get calling package name fail", e)
            }
            if (!packages.isNullOrEmpty()) {
                return packages[0].orEmpty()
            }
        }
        if (activityManager == null) {
            activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }

        if (activityManager == null) {
            return null
        }

        if (lastProcessInfo == null || System.currentTimeMillis() - lastCheckTime > 5000) {
            lastProcessInfo = activityManager!!.runningAppProcesses
            lastCheckTime = System.currentTimeMillis()
        }

        for (processInfo in lastProcessInfo!!) {
            if (processInfo.pid == pid || (pid == -1 && processInfo.uid == uid)) {
                return processInfo.processName
            }
        }

        return ""
    }
}