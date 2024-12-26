package cn.fkj233.deviceemulator.xposed.service

import android.location.Location
import android.os.DeadObjectException
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface
import cn.fkj233.deviceemulator.aidl.mock.IOnMockLocationListenerlInterface
import cn.fkj233.deviceemulator.xposed.mock.gps.GPSMock
import com.github.kyuubiran.ezxhelper.utils.Log

typealias OnMockStatusChangedListener = (Boolean) -> Unit

class MockLocationService : IMockLocationInterface.Stub() {
    private var isMocking = false
    private val listeners = mutableListOf<IOnMockLocationListenerlInterface>()
    private var locationUpdateInterval = 1000L
    private var onMockStatusChangedListener = mutableListOf<OnMockStatusChangedListener>()

    fun notifyLocationChanged(packageName: String, uid: Int, isSystem: Boolean) {
        listeners.forEach {
            if (!it.asBinder().pingBinder()) {
                listeners.remove(it)
                return@forEach
            }

            try {
                it.onRequestLocation(packageName, uid, isSystem)
            } catch (e: DeadObjectException) {
                listeners.remove(it)
            } catch (e: Exception) {
                Log.ix("notifyLocationChanged", e)
            }
        }
    }

    fun addOnMockStatusChangedListener(listener: OnMockStatusChangedListener) {
        onMockStatusChangedListener.add(listener)
    }

    fun removeOnMockStatusChangedListener(listener: OnMockStatusChangedListener) {
        onMockStatusChangedListener.remove(listener)
    }

    override fun registerMockLocationListener(listener: IOnMockLocationListenerlInterface?) {
        runCatching {
            if (listener == null)
                return
            if (listeners.contains(listener))
                return
            listeners.add(listener)
            listener.asBinder().linkToDeath({
                listeners.remove(listener)
            }, 0)
        }.onFailure {
            Log.ix("registerMockLocationListener", it)
        }
    }

    override fun setMockStatus(status: Boolean) {
        isMocking = status
        runCatching {
            onMockStatusChangedListener.forEach { it.invoke(status) }
        }.onFailure {
            Log.ix("setMockStatus", it)
        }
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

    override fun setLocationUpdateInterval(interval: Long) {
        locationUpdateInterval = interval
    }

    override fun getLocationUpdateInterval(): Long {
        return locationUpdateInterval
    }
}