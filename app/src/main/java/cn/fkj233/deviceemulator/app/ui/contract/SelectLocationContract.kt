package cn.fkj233.deviceemulator.app.ui.contract

import cn.fkj233.deviceemulator.app.ui.common.state.IUiEffect
import cn.fkj233.deviceemulator.app.ui.common.state.IUiEvent
import cn.fkj233.deviceemulator.app.ui.common.state.IUiState
import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.model.MapType

class SelectLocationContract {
    sealed class Event : IUiEvent {
    }

    data class State(
        // 是否点击了强制定位
        val isClickForceStartLocation: Boolean,
        // 当前用户自身定位所在的位置
        val currentLocation: LatLng?,
        // 当前手持设备的方向
        val currentRotation: Float,
        // 当前选择的位置
        val currentSelectLocationString: String,
        // 当前选择的位置
        val currentSelectLocation: LatLng?,
        // 地图类型
        val mapType: MapType,
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal data class ShowToast(val msg: String?) : Effect()
    }
}