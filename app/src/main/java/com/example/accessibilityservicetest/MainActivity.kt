package com.example.accessibilityservicetest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.accessibilityservicetest.ui.theme.AccessibilityservicetestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccessibilityservicetestTheme{
                AccessibilityApp()
            }
        }
    }
}


@Composable
fun AccessibilityApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {// Adds space between buttons
            SwipeButton()  // New button to initiate the swipe
        }
    }
}

@Composable
fun SwipeButton() {
    val context = LocalContext.current // 获取当前的Context

    Button(
        onClick = {
            // 检查Accessibility Service是否启动
            val isEnabled = isAccessibilityServiceEnabled(context, MyAccessibilityService::class.java.name)

            if (isEnabled) {
                // 如果已启动，发送广播来执行滑动
                Log.d("SwipeButton", "Button clicked")
                val intent = Intent(MyAccessibilityService.SWIPE_ACTION)

                context.sendBroadcast(intent)
            } else {
                // 如果没有启动，引导用户启动无障碍服务
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "开始上划")
    }
}

// 一个简单的函数来检查是否启用了无障碍服务
fun isAccessibilityServiceEnabled(context: Context, accessibilityService: String): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(accessibilityService) == true
}


