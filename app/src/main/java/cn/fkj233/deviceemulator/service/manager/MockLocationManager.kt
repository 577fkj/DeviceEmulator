package cn.fkj233.deviceemulator.service.manager

import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface

class MockLocationManager(private val service: IMockLocationInterface) {
    fun setMockStatus(status: Boolean) {
        service.setMockStatus(status)
    }

    fun getMockStatus(): Boolean {
        return service.mockStatus
    }

    fun setMockLocation(location: android.location.Location?) {
        service.setMockLocation(location)
    }

    fun getMockLocation(): android.location.Location? {
        return service.mockLocation
    }
}