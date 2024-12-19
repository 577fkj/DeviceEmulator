package cn.fkj233.deviceemulator.xposed.mock.gps

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.math.max

object GPSMock {
    private val mLocation = Location("gps")
    private val mGPSLocation = Location("gps")

    fun setMockLocation(location: Location?) {
        if (location == null) {
            mLocation.reset()
        } else {
            mLocation.set(location)
        }
    }

    fun getMockLocation(): Location? {
        if (mLocation.latitude == 0.0 && mLocation.longitude == 0.0) {
            return null
        }
        return mLocation
    }

    fun getMockGPSLocation(): Location {
        mGPSLocation.set(mLocation)

        if (mGPSLocation.accuracy == 0.0f) {
            mGPSLocation.accuracy = 1.2f
        }

        mGPSLocation.mockGPSFloat()

        if (mGPSLocation.altitude == 0.0) {
            mGPSLocation.altitude = 200.0
        }
        mGPSLocation.altitude += generateFloatValue(5.0f)
        if (mGPSLocation.bearing == 0.0f) {
            mGPSLocation.bearing = 1.0f
        }
        mGPSLocation.bearing += generateFloatValue(1.0f)
        val sdk = Build.VERSION.SDK_INT
        if (sdk >= 26) {
            if (mGPSLocation.bearingAccuracyDegrees == 0.0f) {
                mGPSLocation.bearingAccuracyDegrees = 1.0f
            }
            mGPSLocation.bearingAccuracyDegrees += generateFloatValue(1.0f)

            if (mGPSLocation.speedAccuracyMetersPerSecond == 0.0f) {
                mGPSLocation.speedAccuracyMetersPerSecond = 1.2f
            }
            if (mGPSLocation.verticalAccuracyMeters == 0.0f) {
                mGPSLocation.verticalAccuracyMeters = 3.0f
            }
            mGPSLocation.verticalAccuracyMeters = abs((mGPSLocation.verticalAccuracyMeters + generateFloatValue(1.0f)))
        }

        if (mGPSLocation.speed == 0.0f) {
            mGPSLocation.speed = 1.2f
        }

        val bundle = mGPSLocation.extras ?: Bundle()
        if (!bundle.containsKey("satellites")) {
            bundle.putInt("satellites", 20)
            mGPSLocation.extras = bundle
            mLocation.extras = bundle
        }
        mGPSLocation.time = System.currentTimeMillis()
        mGPSLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        return mGPSLocation
    }

    private fun Location.mockGPSFloat() {
        val locationOffset = accuracy * 10000f
        accuracy = abs(accuracy + generateFloatValue(1f))
        latitude += getMockLocationFloat(locationOffset.toInt(), 8.99E-6f)
        longitude += getMockLocationFloat(locationOffset.toInt(), 1.141E-5f)
    }

    private fun getMockLocationFloat(offset: Int, offset2: Float): Float {
        return if (SecureRandom().nextBoolean()) -1f else 1f * (SecureRandom().nextInt(max(1, offset)) / 10000f) * offset2
    }

    private fun generateFloatValue(maxValue: Float): Float {
        // 确保 maxValue 至少为 1
        val intPart = maxValue.toInt().coerceAtLeast(1)

        // 使用 SecureRandom 生成随机的正负号
        val sign1 = if (SecureRandom().nextBoolean()) -1 else 1
        val sign2 = if (SecureRandom().nextBoolean()) -1 else 1

        // 生成一个随机整数部分和小数部分，并合成随机值
        val randomValue = sign1 * SecureRandom().nextInt(intPart) + sign2 * SecureRandom().nextFloat()

        // 如果生成的随机值绝对值超过 maxValue，返回 maxValue 的一半（随机正负号）
        if (abs(randomValue) > maxValue) {
            val limitedSign = if (SecureRandom().nextBoolean()) -1 else 1
            return limitedSign * (maxValue / 2.0f)
        }

        return randomValue
    }

}