package cn.fkj233.deviceemulator.app.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.fkj233.deviceemulator.app.NavAction
import com.melody.map.gd_compose.GDMap

@Composable
fun SelectLocation(navAction: NavAction) {
    GDMap(
        modifier = Modifier
            .fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
fun SelectLocationPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GDMap(
            modifier = Modifier
                .fillMaxSize(),
        )
        Card(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text("当前位置：")
            }
        }
    }
}