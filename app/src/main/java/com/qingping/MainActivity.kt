package com.qingping

import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val prefs = remember { getSharedPreferences("qingping_stats", MODE_PRIVATE) }
            var total by remember { mutableIntStateOf(prefs.getInt("total", 0)) }
            MaterialTheme {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("清屏", style = MaterialTheme.typography.headlineLarge)
                    Text("广告拦截统计", style = MaterialTheme.typography.titleMedium)
                    Text("成功操作次数：$total", style = MaterialTheme.typography.headlineMedium)
                    Text("当前版本仅处理页面中明确标注的“跳过/关闭”控件，不保证识别所有广告。")
                    Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                        Text("打开无障碍设置")
                    }
                    OutlinedButton(onClick = {
                        total = prefs.getInt("total", 0)
                    }) { Text("刷新统计") }
                    Text("请在系统设置中手动启用“清屏广告识别服务”。")
                }
            }
        }
    }
}
