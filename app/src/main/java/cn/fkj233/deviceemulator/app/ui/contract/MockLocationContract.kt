package cn.fkj233.deviceemulator.app.ui.contract

import android.os.Parcel
import android.os.Parcelable
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

    data class Position(
        val lat: Double,
        val lng: Double,
        val address: String
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString().toString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeDouble(lat)
            parcel.writeDouble(lng)
            parcel.writeString(address)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Position> {
            override fun createFromParcel(parcel: Parcel): Position {
                return Position(parcel)
            }

            override fun newArray(size: Int): Array<Position?> {
                return arrayOfNulls(size)
            }
        }

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
        val mapUiSettings: MapUiSettings,

        // 位置信息
        val position: Position? = null
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal data class ShowToast(val msg: String?) : Effect()
    }
}