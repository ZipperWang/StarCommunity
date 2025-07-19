package com.release.startcommunity.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import coil.compose.rememberAsyncImagePainter
@Composable
fun PersonalProfileScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter("http://api.starcommunity.asia:54321/uploads/default.jpg"),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("张小明", style = MaterialTheme.typography.titleLarge)
            Text("Android 开发者", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 统计数据
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("128", style = MaterialTheme.typography.titleMedium)
                Text("关注", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("512", style = MaterialTheme.typography.titleMedium)
                Text("粉丝", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("26", style = MaterialTheme.typography.titleMedium)
                Text("文章", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = { /* todo编辑资料 */ }) {
                Text("编辑资料")
            }
            OutlinedButton(onClick = {
                /* todo设置 */
                 }) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 简介
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("个人简介", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "热爱 Kotlin 与 Compose，擅长构建现代 Android 应用，喜欢探索交互与架构设计。",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab区域
        val tabs = listOf("动态", "收藏", "关于我")

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                Text("这是动态内容区域", modifier = Modifier.padding(24.dp))
            }
            1 -> {
                Text("这是收藏内容区域", modifier = Modifier.padding(24.dp))
            }
            2 -> {
                Text("这里介绍了更多关于张小明的背景与兴趣爱好。", modifier = Modifier.padding(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}