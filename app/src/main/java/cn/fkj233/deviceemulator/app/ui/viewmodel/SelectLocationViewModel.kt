package cn.fkj233.deviceemulator.app.ui.viewmodel

import cn.fkj233.deviceemulator.app.ui.common.base.BaseViewModel
import cn.fkj233.deviceemulator.app.ui.common.utils.ISensorDegreeListener
import cn.fkj233.deviceemulator.app.ui.common.utils.SensorEventHelper
import cn.fkj233.deviceemulator.app.ui.contract.SelectLocationContract
import cn.fkj233.deviceemulator.app.ui.repo.SelectLocationRepository
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeResult
import com.melody.map.gd_compose.model.MapType
import kotlinx.coroutines.Dispatchers

class SelectLocationViewModel : BaseViewModel<SelectLocationContract.Event, SelectLocationContract.State, SelectLocationContract.Effect>(),
    AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener, ISensorDegreeListener {
    private var mLocationClientSingle: AMapLocationClient? = null

    // POI搜索
    private var mGeocodeSearch : GeocodeSearch? = null

    private val sensorEventHelper = SensorEventHelper()

    override fun createInitialState(): SelectLocationContract.State {
        return SelectLocationContract.State(
            isClickForceStartLocation = false,
            currentLocation = null,
            currentRotation = 0F,
            currentSelectLocation = null,
            currentSelectLocationString = "未知",
            mapType = MapType.NORMAL
        )
    }

    override fun handleEvents(event: SelectLocationContract.Event) {
    }

    init {
        sensorEventHelper.registerSensorListener(this)
    }

    fun startMapLocation() = asyncLaunch(Dispatchers.IO) {
        if(currentState.isClickForceStartLocation) return@asyncLaunch
        setState { copy(isClickForceStartLocation = true) }
        SelectLocationRepository.restartLocation(
            locationClient = mLocationClientSingle,
            listener = this@SelectLocationViewModel
        ) {
            mLocationClientSingle = it
        }
    }

    override fun onCleared() {
        sensorEventHelper.unRegisterSensorListener()
        mLocationClientSingle?.setLocationListener(null)
        mLocationClientSingle?.stopLocation()
        mLocationClientSingle?.onDestroy()
        mLocationClientSingle = null
        super.onCleared()
    }

    override fun onLocationChanged(location: AMapLocation?) {
        setState { copy(isClickForceStartLocation = false) }
        if(null == location) {
            setEffect { SelectLocationContract.Effect.ShowToast("定位失败,请检查定位权限和网络....") }
            return
        }
        val latitude = location.latitude
        val longitude = location.longitude
        val latLon = LatLng(latitude,longitude)
        setState { copy(currentLocation = latLon) }
        doSearchQueryPoi(latLon)
    }

    fun doSearchQueryPoi(latLon: LatLng) = asyncLaunch(Dispatchers.IO) {
        setState { copy(currentSelectLocation = latLon) }
        SelectLocationRepository.doSearchQueryPoi(
            geocodeSearch = mGeocodeSearch,
            moveLatLonPoint = LatLonPoint(latLon.latitude,latLon.longitude),
            listener = this@SelectLocationViewModel
        ) { _, b ->
            mGeocodeSearch = b
        }
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(currentRotation = degree) }
    }

    override fun onRegeocodeSearched(regeocodeResult: RegeocodeResult?, i: Int) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            setState { copy(currentSelectLocationString = regeocodeResult?.regeocodeAddress?.formatAddress ?: "") }
        } else {
            setEffect {
                SelectLocationContract.Effect.ShowToast("获取当前位置信息失败,请检查定位权限和网络....")
            }
        }
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {

    }

    fun setMapType(mapType: MapType) {
        setState { copy(mapType = mapType) }
    }
}