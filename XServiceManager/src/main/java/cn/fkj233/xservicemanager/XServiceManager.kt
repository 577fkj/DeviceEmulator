package cn.fkj233.xservicemanager

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
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
import android.os.Process
import com.github.kyuubiran.ezxhelper.utils.paramCount


typealias ServiceFetcher<T> = (Context) -> T
typealias AddServiceCallback = (String, IBinder) -> Unit

object XServiceManager {
    private const val TAG = "XServiceManager"
    private const val DELEGATE_SERVICE = "clipboard"
    private val SERVICE_FETCHERS: MutableMap<String, ServiceFetcher<IBinder>> = ArrayMap()
    private val sCache: HashMap<String, IBinder> = HashMap()

    private val DESCRIPTOR: String = XServiceManager::class.java.name
    private const val TRANSACTION_getService: Int =
        ('_'.code shl 24) or ('X'.code shl 16) or ('S'.code shl 8) or 'M'.code

    private val packageList: ArrayList<String> = ArrayList()
    private var isWhitelist: Boolean = false

    private var addServiceCallback: AddServiceCallback? = null


    fun setWhiteList(status: Boolean) {
        isWhitelist = status
    }

    fun addPackage(packageName: String) {
        packageList.add(packageName)
    }

    fun addPackage(list: ArrayList<String>) {
        packageList.addAll(list)
    }

    fun removePackage(packageName: String) {
        packageList.remove(packageName)
    }

    fun setAddServiceCallback(cb: AddServiceCallback) {
        addServiceCallback = cb
    }


    private fun getSystemContext(): Context {
        @SuppressLint("PrivateApi") val activityThreadClass =
            Class.forName("android.app.ActivityThread")
        val currentActivityThread = activityThreadClass.getMethod("currentActivityThread")
        val getSystemContext = activityThreadClass.getMethod("getSystemContext")
        val systemContext =
            getSystemContext.invoke(currentActivityThread.invoke(null)) as Context
        return systemContext
    }

    /**
     * Init XServiceManager for system server.
     * Must be called from system_server!
     */
    fun initForSystemServer(keepCheck: Boolean = false) {
        if (!isSystemServerProcess(keepCheck)) {
            Log.d(TAG, "Not system server process, skip inject")
            return
        }
        try {
            findMethod("android.os.ServiceManager") {
                name == "addService" && paramCount == 4
            }.hookBefore {
                val sName = it.args[0] as String
                val service = it.args[1] as IBinder
                if (sName == DELEGATE_SERVICE) {
                    val systemContext = getSystemContext()
                    val customService = XServiceManagerService(systemContext)
                    service.javaClass.findMethod(true) {
                        name == "onTransact"
                    }.hookBefore { tran ->
                        val code = tran.args[0] as Int
                        val data = tran.args[1] as Parcel
                        val reply = tran.args[2] as Parcel?
                        Log.d(TAG, "onTransact $code")
                        if (customService.onTransact(code, data, reply)) {
                            it.result = true
                        }
                    }
                    Log.d(TAG, "inject $DELEGATE_SERVICE success")
                    for ((name, init) in SERVICE_FETCHERS) {
                        try {
                            val s = init(systemContext)
                            addService(name, s, keepCheck)
                            Log.d(TAG, String.format("create %s service success", name))
                        } catch (e: Exception) {
                            Log.e(TAG, String.format("create %s service fail", name), e)
                        }
                    }
                    Log.d(TAG, "All service create success")
                }
                addServiceCallback?.let { cb -> cb(sName, service) }
            }
            Log.d(TAG, "inject addService success")
        } catch (e: Exception) {
            Log.e(TAG, "inject fail", e)
        }
    }

    private fun isSystemServerProcess(keepCheck: Boolean): Boolean {
        if (keepCheck) {
            return true
        }
        if (Process.myUid() != Process.SYSTEM_UID) {
            return false
        }
        try {
            BufferedReader(FileReader(String.format(Locale.getDefault(), "/proc/%d/cmdline", Process.myPid()))).use { r ->
                val processName = r.readLine().trim()
                return "system_server" == processName
            }
        } catch (ignore: IOException) {
            //ignore.printStackTrace();
        }
        return false
    }

    class CallingHelper(private val context: Context) {
        private var activityManager: ActivityManager? = null
        private var lastCheckTime: Long = 0
        private var lastProcessInfo: List<ActivityManager.RunningAppProcessInfo>? = null

        val callingPackageName: String?
            get() = getCallingPackageName(Binder.getCallingUid(), Binder.getCallingPid())

        private fun getCallingPackageName(uid: Int, pid: Int): String? {
            if (uid != -1) {
                var packages: Array<String>? = null
                try {
                    packages = context.packageManager.getPackagesForUid(uid)
                } catch (e: Exception) {
                    Log.e(TAG, "Get calling package name fail", e)
                }
                if (!packages.isNullOrEmpty()) {
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

    class XServiceManagerService(context: Context) {
        private val callingHelper = CallingHelper(context)

        private fun isAllowPackageName(packageName: String?): Boolean {
            if (isWhitelist) {
                return packageList.contains(packageName)
            }
            return !packageList.contains(packageName)
        }

         fun onTransact(
            code: Int,
            data: Parcel,
            reply: Parcel?
        ): Boolean {
            if (code == TRANSACTION_getService) {
                runCatching {
                    data.enforceInterface(DESCRIPTOR)
                    val serviceName = data.readString()
                    if (serviceName == null) {
                        Log.d(TAG, "service name is null")
                        data.setDataPosition(0)
                        reply?.setDataPosition(0)
                        return false
                    }

                    if (Binder.getCallingUid() >= Process.FIRST_APPLICATION_UID) { // System app not check
                        val packageName = callingHelper.callingPackageName
                        if (!isAllowPackageName(packageName)) {
                            Log.d(
                                TAG,
                                String.format("reject %s service %s", packageName, serviceName)
                            )
                            data.setDataPosition(0)
                            reply?.setDataPosition(0)
                            return false
                        }
                        Log.d(
                            TAG,
                            String.format("allow %s get service %s", packageName, serviceName)
                        )
                    }
                    val binder = getServiceInternal(serviceName)

                    Log.d(TAG, String.format("get service %s %s", serviceName, binder))
                    Log.d(TAG, String.format("data %s, reply %s", data, reply))

                    reply?.writeNoException()
                    reply?.writeStrongBinder(binder)
                    return true
                }.onFailure {
                    Log.e(TAG, "Transaction error", it)
                }
                data.setDataPosition(0)
                reply?.setDataPosition(0)
            }
            return false
        }
    }

    private fun getServiceInternal(name: String): IBinder? {
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
    fun <T : IBinder> registerService(name: String, keepCheck: Boolean, serviceFetcher: ServiceFetcher<T>) {
        if (!isSystemServerProcess(keepCheck)) return
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
    fun addService(name: String, service: IBinder, keepCheck: Boolean = false) {
        if (!isSystemServerProcess(keepCheck)) return
        Log.d(TAG, String.format("add service %s %s", name, service))
        sCache[name] = service
    }

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or `null` if the service doesn't exist
     */
    @SuppressLint("Recycle") fun getService(name: String): IBinder? {
        try {
            @SuppressLint("PrivateApi") val serviceManagerClass =
                Class.forName("android.os.ServiceManager")
            val checkService = serviceManagerClass.getMethod(
                "checkService",
                String::class.java
            )
            val delegateService = checkService.invoke(null, DELEGATE_SERVICE) as IBinder
            Objects.requireNonNull(delegateService, "can't not access delegate service")
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeString(name)
                delegateService.transact(TRANSACTION_getService, data, reply, 0)
                reply.readException()
                return reply.readStrongBinder()
            } finally {
                data.recycle()
                reply.recycle()
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

    fun <I : IInterface> getServiceInterface(name: String): I? {
        try {
            val service = getService(name)
            Objects.requireNonNull(service, String.format("can't found %s service", name))
            val descriptor = service?.interfaceDescriptor
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