package cn.fkj233.deviceemulator.service

import android.content.Context
import android.os.IBinder
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface

class DeviceEmulatorService(context: Context) : IDeviceEmulatorInterface.Stub() {
    private var mockDeviceInfoService: MockDeviceInfoService = MockDeviceInfoService(context)
    private var mockLocationService: MockLocationService = MockLocationService(context)

    override fun getMockDeviceInfoIBinder(): IBinder {
        return mockDeviceInfoService.asBinder()
    }

    override fun getMockLocationIBinder(): IBinder {
        return mockLocationService.asBinder()
    }

    override fun getVersion(): Int {
        return BuildConfig.VERSION_CODE
    }
}