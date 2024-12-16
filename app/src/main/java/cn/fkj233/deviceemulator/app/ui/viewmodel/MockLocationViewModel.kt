package cn.fkj233.deviceemulator.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import cn.fkj233.deviceemulator.app.MainApplication
import cn.fkj233.deviceemulator.app.ui.contract.MockLocationContract
import cn.fkj233.deviceemulator.app.ui.common.base.BaseViewModel
import cn.fkj233.deviceemulator.app.ui.common.utils.SDKUtils
import cn.fkj233.deviceemulator.app.ui.common.utils.openAppPermissionSettingPage
import cn.fkj233.deviceemulator.app.ui.common.utils.safeLaunch
import cn.fkj233.deviceemulator.app.ui.repo.MockLocationRepository
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class MockLocationViewModel : BaseViewModel<MockLocationContract.Event, MockLocationContract.State, MockLocationContract.Effect>(), LocationSource, AMapLocationListener {
    override fun createInitialState(): MockLocationContract.State {
        return MockLocationContract.State(
            mapProperties = MockLocationRepository.initMapProperties(),
            mapUiSettings = MockLocationRepository.initMapUiSettings(),
            isShowOpenGPSDialog = false,
            grantLocationPermission = false,
            isOpenGps = null,
            locationLatLng = null,
            position = null
        )
    }

    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null

    override fun handleEvents(event: MockLocationContract.Event) {
        when(event) {
            is MockLocationContract.Event.ShowOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = true) }
            }
            is MockLocationContract.Event.HideOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = false) }
            }
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = SDKUtils.getApplicationContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    fun hideOpenGPSDialog() {
        setEvent(MockLocationContract.Event.HideOpenGPSDialog)
    }

    /**
     * 检查系统GPS开关是否打开
     */
    fun checkGpsStatus() = asyncLaunch(Dispatchers.IO) {
        val isOpenGps = checkGPSIsOpen()
        setState { copy(isOpenGps = isOpenGps) }
        if(!isOpenGps) {
            setEvent(MockLocationContract.Event.ShowOpenGPSDialog)
        } else {
            hideOpenGPSDialog()
        }
    }

    /**
     * 手机开了GPS，app没有授予权限
     */
    fun handleNoGrantLocationPermission() {
        setState { copy(grantLocationPermission = false) }
        setEvent(MockLocationContract.Event.ShowOpenGPSDialog)
    }

    fun handleGrantLocationPermission() {
        setState { copy(grantLocationPermission = true) }
        checkGpsStatus()
    }

    fun openGPSPermission(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if(checkGPSIsOpen()) {
            // 已打开系统GPS，APP还没授权，跳权限页面
            openAppPermissionSettingPage()
        } else {
            // 打开系统GPS开关页面
            launcher.safeLaunch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }


    fun startMapLocation() {
        MockLocationRepository.initAMapLocationClient(mLocationClient,this) { client, option->
            mLocationClient = client
            mLocationOption = option
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        MockLocationRepository.handleLocationChange(amapLocation) { aMapLocation, msg ->
            if(null != aMapLocation) {
                val delayTime = if(null == currentState.locationLatLng) 100L else 0L
                setState {
                    copy(locationLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude))
                }
                asyncLaunch {
                    // 首次直接显示，高德地图【默认小蓝点】会【有点闪烁】，延迟一下再回调
                    delay(delayTime)
                    // 显示系统小蓝点
                    mListener?.onLocationChanged(aMapLocation)
                }
            } else {
                setEffect { MockLocationContract.Effect.ShowToast(msg) }
            }
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if(checkGPSIsOpen() && currentState.grantLocationPermission) {
            startMapLocation()
        }
    }

    override fun deactivate() {
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
        mListener = null
    }

    override fun onCleared() {
        mLocationClient?.onDestroy()
        mLocationClient = null
        super.onCleared()
    }

    fun setPosition(position: MockLocationContract.Position) {
        synchronized(this) {
            setState {
                copy(
                    position = position
                )
            }
        }
    }
}