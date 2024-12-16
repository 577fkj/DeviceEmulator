package cn.fkj233.deviceemulator.app.ui.contract

import cn.fkj233.deviceemulator.app.ui.common.state.IUiEffect
import cn.fkj233.deviceemulator.app.ui.common.state.IUiEvent
import cn.fkj233.deviceemulator.app.ui.common.state.IUiState
import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.map.gd_compose.poperties.MapUiSettings

class MockLocationContract {
    sealed class Event : IUiEvent {
        object ShowOpenGPSDialog : Event()
        object HideOpenGPSDialog : Event()
    }

    data class State(
        // 是否打开了系统GPS权限
        val isOpenGps: Boolean?,
        // 是否显示打开GPS的确认弹框
        val isShowOpenGPSDialog: Boolean,
        // App是否打开了定位权限
        val grantLocationPermission:Boolean,
        // 当前位置的经纬度
        val locationLatLng: LatLng?,
        val mapProperties: MapProperties,
        val mapUiSettings: MapUiSettings
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal data class ShowToast(val msg: String?) : Effect()
    }
}