package cn.fkj233.deviceemulator.xposed

import android.annotation.SuppressLint
import android.os.Binder
import android.os.Process
import cn.fkj233.deviceemulator.service.DeviceEmulatorService
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import cn.fkj233.deviceemulator.service.manager.MockDeviceInfoManager
import cn.fkj233.deviceemulator.service.manager.MockLocationManager

object ServiceHelper {
    private var deviceEmulatorService: DeviceEmulatorService = DeviceEmulatorService()

    private var deviceEmulatorManager: DeviceEmulatorManager = DeviceEmulatorManager(deviceEmulatorService)
    private var mockLocationManager: MockLocationManager = MockLocationManager(deviceEmulatorManager.getMockLocationService())
    private var mockDeviceInfoManager: MockDeviceInfoManager = MockDeviceInfoManager(deviceEmulatorService.getMockDeviceInfoService())

    fun getDeviceEmulatorService(): DeviceEmulatorService {
        return deviceEmulatorService
    }

    fun getDeviceEmulatorManager(): DeviceEmulatorManager {
        return deviceEmulatorManager
    }

    fun getMockLocationManager(): MockLocationManager {
        return mockLocationManager
    }

    fun getMockDeviceInfoManager(): MockDeviceInfoManager {
        return mockDeviceInfoManager
    }

    fun isMocking(): Boolean {
        return mockLocationManager.getMockStatus()
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