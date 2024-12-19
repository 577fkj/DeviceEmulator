package cn.fkj233.deviceemulator.app.pref

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonPref
import com.google.gson.annotations.SerializedName

object LocationData : KotprefModel() {
    var historyLocation by gsonPref(LocationList())
}

fun LocationList.add(location: LocationInfo, index: Int = 0) {
    val list = this.list.toMutableList()
    list.add(index, location)
    LocationData.historyLocation = LocationList(list)
}

fun LocationList.remove(index: Int) {
    val list = this.list.toMutableList()
    list.removeAt(index)
    LocationData.historyLocation = LocationList(list)
}

data class LocationList(
    @SerializedName("list")
    val list: List<LocationInfo> = arrayListOf()
)

data class LocationInfo(
    @SerializedName("latitude")
    val latitude: Double = 0.0,
    @SerializedName("longitude")
    val longitude: Double = 0.0,
    @SerializedName("address")
    val address: String = ""
)