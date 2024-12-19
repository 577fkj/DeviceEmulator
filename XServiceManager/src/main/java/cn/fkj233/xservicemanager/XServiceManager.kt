package cn.fkj233.xservicemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.IPackageManager
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.Build
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
import java.util.Objects
import android.os.Process
import com.github.kyuubiran.ezxhelper.utils.paramCount


typealias ServiceFetcher<T> = (Context) -> T
typealias AddServiceCallback = (String, IBinder) -> Unit

object XServiceManager {
    private const val TAG = "XServiceManager"
    private const val DELEGATE_SERVICE = Context.CLIPBOARD_SERVICE
    private val SERVICE_FETCHERS: MutableMap<String, ServiceFetcher<IBinder>> = ArrayMap()
    private val sCache: HashMap<String, IBinder> = HashMap()

    private val DESCRIPTOR: String = XServiceManager::class.java.name
    private const val TRANSACTION_getService: Int = ('_'.code shl 24) or ('X'.code shl 16) or ('S'.code shl 8) or 'M'.code

    private val packageList: ArrayList<String> = ArrayList()
    private var isWhitelist: Boolean = false

    private var addServiceCallback: AddServiceCallback? = null

    private var pms: IPackageManager? = null


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
            }.hookBefore { addService ->
                val sName = addService.args[0] as String
                val service = addService.args[1] as IBinder

                if (sName == DELEGATE_SERVICE) {
                    val systemContext = getSystemContext()
                    val customService = XServiceManagerService()
                    service.javaClass.findMethod(true) {
                        name == "onTransact"
                    }.hookBefore { tran ->
                        val code = tran.args[0] as Int
                        val data = tran.args[1] as Parcel
                        val reply = tran.args[2] as Parcel?
                        if (customService.onTransact(code, data, reply)) {
                            tran.result = true
                        }
                    }
                    Log.d(TAG, "inject $DELEGATE_SERVICE success")
                    for ((name, init) in SERVICE_FETCHERS) {
                        try {
                            val s = init(systemContext)
                            addService(name, s, keepCheck)
                            Log.d(TAG, "create $name service success")
                        } catch (e: Exception) {
                            Log.e(TAG, "create $name service fail", e)
                        }
                    }
                    Log.d(TAG, "All service create success")
                } else if (sName == "package") {
                    pms = service as IPackageManager
                    Log.d(TAG, "get $sName success")
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
            BufferedReader(FileReader("/proc/${Process.myPid()}/cmdline")).use { r ->
                val processName = r.readLine().trim()
                return "system_server" == processName
            }
        } catch (ignore: IOException) {
            //ignore.printStackTrace();
        }
        return false
    }

    class CallingHelper {
        val callingPackageName: Array<String>?
            get() {
                val uid = Binder.getCallingUid()
                return getPackageNameForUidCompat(uid)
            }

        fun getPackageNameForUidCompat(uid: Int): Array<String>? {
            return pms?.getPackagesForUid(uid)
        }

        fun getInstalledApplicationsCompat(flags: Long, userId: Int): List<ApplicationInfo>? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pms?.getInstalledApplications(flags, userId)
            } else {
                pms?.getInstalledApplications(flags.toInt(), userId)
            }?.list
        }

        fun getPackageUidCompat(packageName: String, flags: Long, userId: Int): Int? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pms?.getPackageUid(packageName, flags, userId)
            } else {
                pms?.getPackageUid(packageName, flags.toInt(), userId)
            }
        }

        fun getPackageInfoCompat(packageName: String, flags: Long, userId: Int): PackageInfo? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pms?.getPackageInfo(packageName, flags, userId)
            } else {
                pms?.getPackageInfo(packageName, flags.toInt(), userId)
            }
        }
    }

    class XServiceManagerService {
        private val callingHelper = CallingHelper()

        private fun isAllowPackageName(packageName: Array<String>?): Boolean {
            val result = packageName?.any { it in packageList } ?: false
            if (isWhitelist) {
                return result
            }
            return !result
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

                    val uid = Binder.getCallingUid()
                    if (uid == Process.SHELL_UID || uid >= Process.FIRST_APPLICATION_UID) { // System app not check but shell check
                        val packageName = callingHelper.callingPackageName
                        if (!isAllowPackageName(packageName)) {
                            Log.d(TAG, "reject ${packageName?.contentToString()} service $serviceName")
                            data.setDataPosition(0)
                            reply?.setDataPosition(0)
                            return false
                        }
                        Log.d(TAG, "allow ${packageName?.contentToString()} get service $serviceName")
                    }
                    val binder = getServiceInternal(serviceName)

                    reply?.writeNoException()
                    reply?.writeStrongBinder(binder)
                    return true
                }.onFailure {
                    Log.e(TAG, "Transaction error", it)
                }
                Log.d(TAG, "Transaction fail")
                data.setDataPosition(0)
                reply?.setDataPosition(0)
            }
            return false
        }
    }

    private fun getServiceInternal(name: String): IBinder? {
        val binder = sCache[name]
        Log.d(TAG, "get service $name $binder")
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
        Log.d(TAG, "register service $name $serviceFetcher")
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
        Log.d(TAG, "add service $name $service")
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
                val service = reply.readStrongBinder()
                return service
            } finally {
                data.recycle()
                reply.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "get $name service error", if (e is InvocationTargetException) e.cause else e)
            return null
        }
    }

    fun <I : IInterface> getServiceInterface(name: String): I? {
        try {
            val service = getService(name)
            Objects.requireNonNull(service, "can't found $name service")
            val descriptor = service?.interfaceDescriptor
            val stubClass = XServiceManager::class.java.classLoader!!.loadClass("$descriptor\$Stub")
            @Suppress("UNCHECKED_CAST")
            return stubClass.getMethod("asInterface", IBinder::class.java)
                .invoke(null, service) as I
        } catch (e: Exception) {
            Log.e(TAG, "get $name service error", if (e is InvocationTargetException) e.cause else e)
            return null
        }
    }
}