// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package cn.fkj233.deviceemulator.app.ui.repo

import android.content.Context
import android.location.LocationManager
import cn.fkj233.deviceemulator.app.ui.common.utils.SDKUtils
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery

/**
 * SelectLocationRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:50
 */
object SelectLocationRepository {
    inline fun restartLocation(locationClient: AMapLocationClient?, listener: AMapLocationListener, block: (AMapLocationClient) -> Unit) {
        // 必须先停止，再重新初始化，否则会报错：【用户MD5安全码不通过】
        locationClient?.setLocationListener(null)
        locationClient?.stopLocation()
        val newLocationClientSingle = AMapLocationClient(SDKUtils.getApplicationContext())
        newLocationClientSingle.setLocationListener(listener)
        // 给定位客户端对象设置定位参数
        newLocationClientSingle.setLocationOption(AMapLocationClientOption().apply {
            // 获取一次定位结果
            isOnceLocation = true
            // 设置为高精度定位模式
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        })
        block.invoke(
            newLocationClientSingle.apply {
                startLocation()
            }
        )
    }

    inline fun doSearchQueryPoi(
        geocodeSearch: GeocodeSearch?,
        moveLatLonPoint: LatLonPoint?,
        listener: GeocodeSearch.OnGeocodeSearchListener,
        block: (RegeocodeQuery, GeocodeSearch) -> Unit
    ) {
        if (moveLatLonPoint != null) {
            // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            val newSearch2: GeocodeSearch
            if (null == geocodeSearch) {
                newSearch2 = GeocodeSearch(SDKUtils.getApplicationContext())
                newSearch2.setOnGeocodeSearchListener(listener)
            } else {
                newSearch2 = geocodeSearch
            }

            val query = RegeocodeQuery(moveLatLonPoint, 200f, GeocodeSearch.AMAP)
            newSearch2.getFromLocationAsyn(query)

            block.invoke(query, newSearch2)
        }
    }
}