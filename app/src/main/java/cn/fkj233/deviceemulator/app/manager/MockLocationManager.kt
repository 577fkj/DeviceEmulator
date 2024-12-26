package cn.fkj233.deviceemulator.app.manager

import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface

class MockLocationManager private constructor(private val service: IMockLocationInterface) {
    companion object {
        private var instance: MockLocationManager? = null

        fun getDefault(): MockLocationManager {
            synchronized(MockLocationManager::class.java) {
                instance = if (instance != null) {
                    MockLocationManager(DeviceEmulatorManager.getDefault().getMockLocationService())
                } else {
                    MockLocationManager(IMockLocationInterface.Default())
                }
            }
            return instance!!
        }
    }


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