package com.example.screenstream

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class ControlAccessibilityService : AccessibilityService() {
    companion object { var instance: ControlAccessibilityService? = null }

    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }

    fun handleControl(message: JSONObject) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val display = resources.displayMetrics
        val width = display.widthPixels.toFloat()
        val height = display.heightPixels.toFloat()
        when (message.optString("type")) {
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "recents" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            "tap" -> {
                val x = (message.optDouble("x", 0.5) * width).toFloat()
                val y = (message.optDouble("y", 0.5) * height).toFloat()
                dispatchGesture(path(x, y, x, y), null, null)
            }
            "swipe" -> {
                val x1 = (message.optDouble("x1", 0.5) * width).toFloat()
                val y1 = (message.optDouble("y1", 0.5) * height).toFloat()
                val x2 = (message.optDouble("x2", 0.5) * width).toFloat()
                val y2 = (message.optDouble("y2", 0.5) * height).toFloat()
                dispatchGesture(path(x1, y1, x2, y2), null, null)
            }
        }
    }

    private fun path(x1: Float, y1: Float, x2: Float, y2: Float): GestureDescription {
        val p = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val duration = if (x1 == x2 && y1 == y2) 80L else 250L
        return GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(p, 0, duration)).build()
    }
}
