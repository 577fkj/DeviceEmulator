package cn.fkj233.deviceemulator.app.pref

import android.location.Location
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonPref
import com.google.gson.annotations.SerializedName

object AddressData : KotprefModel() {
    var historyAddress by gsonPref(AddressList())
}

fun AddressList.add(location: AddressInfo, index: Int = 0) {
    val list = this.list.toMutableList()
    list.add(index, location)
    AddressData.historyAddress = AddressList(list)
}

fun AddressList.remove(index: Int) {
    val list = this.list.toMutableList()
    list.removeAt(index)
    AddressData.historyAddress = AddressList(list)
}

data class AddressList(
    @SerializedName("list")
    val list: List<AddressInfo> = arrayListOf()
)

data class AddressInfo(
    @SerializedName("latitude")
    val latitude: Double = 0.0,
    @SerializedName("longitude")
    val longitude: Double = 0.0,
    @SerializedName("address")
    val address: String = ""
)

fun AddressInfo.convertToLocation(): Location {
    return Location("gps").also {
        it.latitude = this.latitude
        it.longitude = this.longitude
    }
}