package cn.fkj233.deviceemulator.service

import android.content.Context
import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface

class MockLocationService(val context: Context) : IMockLocationInterface.Stub() {
    override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float, aDouble: Double, aString: String?) {
        TODO("Not yet implemented")
    }

}