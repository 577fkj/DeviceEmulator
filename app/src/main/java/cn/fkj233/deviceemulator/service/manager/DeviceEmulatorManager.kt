package cn.fkj233.deviceemulator.service.manager

import android.content.Context
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockDeviceInfoInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface

class DeviceEmulatorManager(private val service: IDeviceEmulatorInterface) {
    companion object {
        const val SERVICE_NAME = "device_emulator"
    }

    fun getMockDeviceInfoService(): IMockDeviceInfoInterface {
        return IMockDeviceInfoInterface.Stub.asInterface(service.mockDeviceInfoIBinder)
    }

    fun getMockLocationService(): IMockLocationInterface {
        return IMockLocationInterface.Stub.asInterface(service.mockLocationIBinder)
    }

    fun getVersion(): Int {
        return service.version
    }
}