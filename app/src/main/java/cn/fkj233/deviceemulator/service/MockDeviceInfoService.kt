package cn.fkj233.deviceemulator.service

import android.content.Context
import cn.fkj233.deviceemulator.mock.IMockDeviceInfoInterface

class MockDeviceInfoService(val context: Context) : IMockDeviceInfoInterface.Stub() {
    override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float, aDouble: Double, aString: String?) {
        TODO("Not yet implemented")
    }
}