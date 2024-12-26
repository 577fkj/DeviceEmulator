package cn.fkj233.deviceemulator.xposed.service

import android.content.Context
import android.os.IBinder
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockDeviceInfoInterface
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface

class DeviceEmulatorService : IDeviceEmulatorInterface.Stub() {
    private var mockDeviceInfoService: MockDeviceInfoService = MockDeviceInfoService()
    private var mockLocationService: MockLocationService = MockLocationService()

    fun getMockDeviceInfoService(): MockDeviceInfoService {
        return mockDeviceInfoService
    }

    fun getMockLocationService(): MockLocationService {
        return mockLocationService
    }

    override fun getMockDeviceInfo(): IMockDeviceInfoInterface {
        return mockDeviceInfoService
    }

    override fun getMockLocation(): IMockLocationInterface {
        return mockLocationService
    }

    override fun getVersion(): Int {
        return BuildConfig.VERSION_CODE
    }
}