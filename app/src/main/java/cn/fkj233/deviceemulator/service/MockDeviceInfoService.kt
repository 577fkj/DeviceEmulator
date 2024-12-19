package cn.fkj233.deviceemulator.service

import android.content.Context
import cn.fkj233.deviceemulator.aidl.mock.IMockDeviceInfoInterface

class MockDeviceInfoService : IMockDeviceInfoInterface.Stub() {
    override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float, aDouble: Double, aString: String?) {
        TODO("Not yet implemented")
    }
}