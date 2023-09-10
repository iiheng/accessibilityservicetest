package com.example.accessibilityservicetest

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {
    companion object {
        const val SWIPE_ACTION = "com.example.kitlintest2.SWIPE_ACTION"
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("connect","service已经连接")
        val filter = IntentFilter(SWIPE_ACTION)
        registerReceiver(swipeReceiver, filter)
    }


    private val swipeReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SWIPE_ACTION) {
                Log.d("SwipeButton", "接收到广播了");
                performSwipeAction()
            }
        }
    }


    // Simulates an L-shaped drag path: 200 pixels right, then 200 pixels down.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun performSwipeAction() {
        val path = Path().apply {
            moveTo(500f, 1500f) // Starting point (x, y)
            quadTo(300f, 1000f, 500f, 500f)
        }

        val gestureDescription = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 1000L, 1000L))
            .build()

        dispatchGesture(gestureDescription, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(swipeReceiver)
    }
}
