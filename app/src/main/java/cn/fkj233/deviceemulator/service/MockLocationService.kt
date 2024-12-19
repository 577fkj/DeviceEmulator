package cn.fkj233.deviceemulator.service

import android.content.Context
import android.location.Location
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface
import cn.fkj233.deviceemulator.xposed.mock.gps.GPSMock

class MockLocationService : IMockLocationInterface.Stub() {
    private var isMocking = false

    override fun setMockStatus(status: Boolean) {
        isMocking = status
    }

    override fun getMockStatus(): Boolean {
        return isMocking
    }

    override fun setMockLocation(location: Location?) {
        GPSMock.setMockLocation(location)
    }

    override fun getMockLocation(): Location? {
        return GPSMock.getMockLocation()
    }
}