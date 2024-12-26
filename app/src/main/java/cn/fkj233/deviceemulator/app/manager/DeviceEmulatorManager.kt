package cn.fkj233.deviceemulator.app.manager

import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockDeviceInfoInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface
import cn.fkj233.xservicemanager.XServiceManager

class DeviceEmulatorManager private constructor(private val service: IDeviceEmulatorInterface) {
    companion object {
        const val SERVICE_NAME = "device_emulator"
        private var instance: DeviceEmulatorManager? = null

        fun getDefault(): DeviceEmulatorManager {
            synchronized(DeviceEmulatorManager::class.java) {
                if (instance == null) {
                    val service = XServiceManager.getService(SERVICE_NAME)
                    instance = if (service != null) {
                        DeviceEmulatorManager(IDeviceEmulatorInterface.Stub.asInterface(service))
                    } else {
                        DeviceEmulatorManager(IDeviceEmulatorInterface.Default())
                    }
                }
            }
            return instance!!
        }
    }

    fun getMockDeviceInfoService(): IMockDeviceInfoInterface {
        return service.mockDeviceInfo
    }

    fun getMockLocationService(): IMockLocationInterface {
        return service.mockLocation
    }

    fun getVersion(): Int {
        return service.version
    }
}