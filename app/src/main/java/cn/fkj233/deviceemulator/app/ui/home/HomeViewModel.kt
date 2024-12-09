package cn.fkj233.deviceemulator.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.fkj233.deviceemulator.app.MainActivity
import cn.fkj233.deviceemulator.common.InetAddressHelper

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        val a = InetAddressHelper.getIpv4InetAddress("172.16.0.1")
        val b = InetAddressHelper.getIpv6InetAddress("2400:3200::1")
        val c = InetAddressHelper.getIpv6InetAddress("7da::")
        value = "IPv4: $a\nIPv6: $b\nIPv6: $c\nService: ${MainActivity.service}"
    }
    val text: LiveData<String> = _text
}