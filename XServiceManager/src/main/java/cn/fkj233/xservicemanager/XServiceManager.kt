package cn.fkj233.xservicemanager

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import android.util.ArrayMap
import android.util.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.Locale
import java.util.Objects


object XServiceManager {
    const val TAG: String = "XServiceManager"
    const val DELEGATE_SERVICE: String = "clipboard"
    val SERVICE_FETCHERS: MutableMap<String, ServiceFetcher<Binder>> = ArrayMap()
    val sCache: HashMap<String?, IBinder> = HashMap()

    val DESCRIPTOR: String = XServiceManager::class.java.name
    const val TRANSACTION_getService: Int =
        ('_'.code shl 24) or ('X'.code shl 16) or ('S'.code shl 8) or 'M'.code

    val packageList: ArrayList<String?> = ArrayList()
    var isWhitelist: Boolean = false

    interface ServiceFetcher<T : Binder> {
        fun createService(ctx: Context): T
    }

    fun setWhiteList(status: Boolean) {
        isWhitelist = status
    }

    fun addPackage(packageName: String?) {
        packageList.add(packageName)
    }

    fun addPackage(list: ArrayList<String?>?) {
        packageList.addAll(list!!)
    }

    fun removePackage(packageName: String?) {
        packageList.remove(packageName)
    }

    /**
     * Init XServiceManager for system server.
     * Must be called from system_server!
     */
    fun initForSystemServer() {
        if (!isSystemServerProcess()) return
        try {
            findMethod("android.os.ServiceManager") {
                name == "addService"
            }.hookBefore {
                val sName = it.args[0] as String
                val service = it.args[1] as IBinder
                if (sName == DELEGATE_SERVICE) {
                    service.javaClass.findMethod {
                        name == "onTransact"
                    }.hookBefore { tran ->
                        val code = tran.args[0] as Int
                        val data = tran.args[1] as Parcel
                        val reply = tran.args[2] as Parcel?
                        if (myTransact(code, data, reply)) {
                            it.result = true
                        }
                    }
                }
            }
            Log.d(TAG, "inject success")
        } catch (e: Exception) {
            Log.e(TAG, "inject fail", e)
        }
    }

    private fun isSystemServerProcess(): Boolean {
        if (Process.myUid() !== Process.SYSTEM_UID) {
            return false
        }
        try {
            BufferedReader(
                FileReader(
                    java.lang.String.format(
                        Locale.getDefault(),
                        "/proc/%d/cmdline",
                        Process.myPid()
                    )
                )
            ).use { r ->
                val processName = r.readLine().trim { it <= ' ' }
                return "system_server" == processName
            }
        } catch (ignore: IOException) {
            //ignore.printStackTrace();
        }
        return false
    }

    static
    class CallingHelper(private val context: Context) {
        private var activityManager: ActivityManager? = null
        private var lastCheckTime: Long = 0
        private var lastProcessInfo: List<ActivityManager.RunningAppProcessInfo>? = null

        val callingPackageName: String?
            get() = getCallingPackageName(Binder.getCallingUid(), Binder.getCallingPid())

        fun getCallingPackageName(uid: Int, pid: Int): String? {
            if (uid != -1) {
                var packages: Array<String?>? = null
                try {
                    packages = context.packageManager.getPackagesForUid(uid)
                } catch (e: Exception) {
                    Log.e(TAG, "Get calling package name fail", e)
                }
                if (packages != null && packages.size >= 1) {
                    return packages[0]
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

            return null
        }
    }

    private static
    class BinderDelegateService(
        private val systemService: IBinder,
        private val customService: IBinder,
        context: Context
    ) :
        Binder() {
        private val callingHelper = CallingHelper(context)

        fun isAllowPackageName(packageName: String?): Boolean {
            if (isWhitelist) {
                return packageList.contains(packageName)
            }
            return !packageList.contains(packageName)
        }

        @Throws(RemoteException::class)
        override fun onTransact(
            code: Int,
            @NonNull data: Parcel,
            reply: Parcel?,
            flags: Int
        ): Boolean {
            if (code == TRANSACTION_getService) {
                val packageName = callingHelper.callingPackageName
                if (!isAllowPackageName(packageName)) {
                    Log.d(
                        TAG,
                        String.format("reject %s service %s", packageName, data.readString())
                    )
                    return false
                }
                return customService.transact(code, data, reply, flags)
            }
            return systemService.transact(code, data, reply, flags)
        }
    }

    private static
    class XServiceManagerService : Binder() {
        @Throws(RemoteException::class)
        override fun onTransact(
            code: Int,
            @NonNull data: Parcel,
            reply: Parcel?,
            flags: Int
        ): Boolean {
            val descriptor = DESCRIPTOR
            when (code) {
                INTERFACE_TRANSACTION -> {
                    reply!!.writeString(descriptor)
                    return true
                }

                TRANSACTION_getService -> {
                    data.enforceInterface(descriptor)
                    val name = data.readString()
                    reply!!.writeNoException()
                    val binder = getServiceInternal(name)
                    reply.writeStrongBinder(binder)
                    return true
                }

                else -> {
                    return super.onTransact(code, data, reply, flags)
                }
            }
        }
    }

    private fun getServiceInternal(name: String?): IBinder? {
        val binder = sCache[name]
        Log.d(TAG, String.format("get service %s %s", name, binder))
        return binder
    }

    /**
     * Register a new @a serviceFetcher called @a name into the service
     * Services registered differently from [.addService] will be delayed
     * until the clipboard service is created. If your service depends on the core service of the
     * system or the context should be added in this way.
     * Must be called from system_server!
     *
     * @param name           the name of the new service
     * @param serviceFetcher the service fetcher object
     */
    fun <T : Binder?> registerService(name: String, serviceFetcher: ServiceFetcher<T>) {
        if (!isSystemServerProcess()) return
        Log.d(TAG, String.format("register service %s %s", name, serviceFetcher))
        SERVICE_FETCHERS[name] = serviceFetcher
    }

    /**
     * Place a new @a service called @a name into the service
     * manager.
     * Must be called from system_server!
     *
     * @param name    the name of the new service
     * @param service the service object
     */
    fun addService(name: String?, service: IBinder) {
        if (!isSystemServerProcess()) return
        Log.d(TAG, String.format("add service %s %s", name, service))
        sCache[name] = service
    }

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or `null` if the service doesn't exist
     */
    fun getService(name: String?): IBinder? {
        try {
            @SuppressLint("PrivateApi") val ServiceManagerClass =
                Class.forName("android.os.ServiceManager")
            val checkService = ServiceManagerClass.getMethod(
                "checkService",
                String::class.java
            )
            val delegateService = checkService.invoke(null, DELEGATE_SERVICE) as IBinder
            Objects.requireNonNull(delegateService, "can't not access delegate service")
            val _data = Parcel.obtain()
            val _reply = Parcel.obtain()
            try {
                _data.writeInterfaceToken(DESCRIPTOR)
                _data.writeString(name)
                delegateService.transact(TRANSACTION_getService, _data, _reply, 0)
                _reply.readException()
                return _reply.readStrongBinder()
            } finally {
                _data.recycle()
                _reply.recycle()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                String.format("get %s service error", name),
                if (e is InvocationTargetException) e.cause else e
            )
            return null
        }
    }

    fun <I : IInterface?> getServiceInterface(name: String?): I? {
        try {
            val service = getService(name)
            Objects.requireNonNull(service, String.format("can't found %s service", name))
            val descriptor = service!!.interfaceDescriptor
            val stubClass = XServiceManager::class.java.classLoader!!.loadClass("$descriptor\$Stub")
            @Suppress("UNCHECKED_CAST")
            return stubClass.getMethod("asInterface", IBinder::class.java)
                .invoke(null, service) as I
        } catch (e: Exception) {
            Log.e(
                TAG,
                String.format("get %s service error", name),
                if (e is InvocationTargetException) e.cause else e
            )
            return null
        }
    }

}