package cn.fkj233.deviceemulator.app.ui.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fkj233.deviceemulator.BuildConfig
import cn.fkj233.deviceemulator.app.MainActivity
import cn.fkj233.deviceemulator.app.ui.utils.XposedData

@Composable
fun Home() {

    val data = mapOf(
        "设备" to "${Build.MANUFACTURER} ${Build.BRAND} ${Build.MODEL} (${Build.DEVICE})",
        "系统版本" to "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})",
        "系统架构" to Build.SUPPORTED_ABIS.joinToString(", "),
        "管理器包名" to BuildConfig.APPLICATION_ID,
        "管理器版本" to "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        "服务版本" to MainActivity.service?.version.toString()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
    ) {
        Column{
            IsActive(XposedData.isActive(), MainActivity.service != null)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(Color.Transparent)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 20.dp),
            ) {
                data.forEach { (index, value) ->
                    Column(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    ) {
                        Text(index, fontSize = 18.sp)
                        Text(value)
                    }
                }
            }
        }
    }
}

@Composable
fun IsActive(active: Boolean, service: Boolean) {
    var icon = Icons.Filled.Done
    var color = Color.Green

    if (!active) {
        icon = Icons.Filled.Clear
        color = Color.Red
    } else if (!service) {
        icon = Icons.Filled.Warning
        color = Color.Yellow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Active",
                modifier = Modifier.padding(10.dp)
            )
            Column {
                if (active) {
                    Text(text = "Active")
                } else {
                    Text(text = "Not Active")
                }
                Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomePreview() {
    Home()
}