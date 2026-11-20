package com.autotapper.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlin.random.Random

class AutoClickService : AccessibilityService() {
    companion object {
        const val ACTION_START = "com.autotapper.app.START"
        const val ACTION_STOP = "com.autotapper.app.STOP"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_START -> startTapping()
                ACTION_STOP -> stopTapping()
            }
        }
    }

    private val tapTask = object : Runnable {
        override fun run() {
            if (!running) return
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val baseX = prefs.getInt("x", 500).toFloat()
            val baseY = prefs.getInt("y", 900).toFloat()
            val radius = prefs.getInt("radius", 12)
            val dx = if (radius == 0) 0f else Random.nextInt(-radius, radius + 1).toFloat()
            val dy = if (radius == 0) 0f else Random.nextInt(-radius, radius + 1).toFloat()
            val path = Path().apply { moveTo((baseX + dx).coerceAtLeast(0f), (baseY + dy).coerceAtLeast(0f)) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 55))
                .build()
            dispatchGesture(gesture, null, null)
            handler.postDelayed(this, prefs.getLong("interval", 1000L).coerceAtLeast(250L))
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter().apply {
            addAction(ACTION_START)
            addAction(ACTION_STOP)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    private fun startTapping() {
        if (running) return
        running = true
        handler.post(tapTask)
    }

    private fun stopTapping() {
        running = false
        handler.removeCallbacks(tapTask)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = stopTapping()

    override fun onDestroy() {
        stopTapping()
        try { unregisterReceiver(receiver) } catch (_: IllegalArgumentException) { }
        super.onDestroy()
    }
}
