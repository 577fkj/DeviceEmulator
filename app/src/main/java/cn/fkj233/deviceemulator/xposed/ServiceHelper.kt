package cn.fkj233.deviceemulator.xposed

import android.annotation.SuppressLint
import android.os.Binder
import android.os.Process
import cn.fkj233.deviceemulator.xposed.service.DeviceEmulatorService

object ServiceHelper {
    private val deviceEmulatorService: DeviceEmulatorService = DeviceEmulatorService()
    private val mockLocationService = deviceEmulatorService.getMockLocationService()
    private val mockDeviceInfoService = deviceEmulatorService.getMockDeviceInfoService()

    fun getDeviceEmulatorService() = deviceEmulatorService
    fun getMockLocationService() = mockLocationService
    fun getMockDeviceInfoService() = mockDeviceInfoService


    fun notifyRequestLocation(packageName: String, uid: Int, isSystem: Boolean) {
        mockLocationService.notifyLocationChanged(packageName, uid, isSystem)
    }

    fun isMocking(): Boolean {
        return mockLocationService.mockStatus
    }

    fun isHook(): Boolean {
        val uid = Binder.getCallingUid()
        return isMocking() && (isShell(uid) || !isSystem(uid))
    }

    fun isSystem(uid: Int = Binder.getCallingUid()): Boolean {
        return uid < Process.FIRST_APPLICATION_UID
    }

    @SuppressLint("InlinedApi")
    fun isShell(uid: Int = Binder.getCallingUid()): Boolean {
        return uid == Process.SHELL_UID
    }
}