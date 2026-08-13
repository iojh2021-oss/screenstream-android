package com.example.screenstream
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
class ControlAccessibilityService : AccessibilityService() {
    companion object { var instance: ControlAccessibilityService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
