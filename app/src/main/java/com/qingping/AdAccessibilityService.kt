package com.qingping

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AdAccessibilityService : AccessibilityService() {
    private val targets = listOf("跳过广告", "跳过", "关闭广告", "关闭")
    private var lastActionAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val now = System.currentTimeMillis()
        if (now - lastActionAt < 1200) return
        val root = rootInActiveWindow ?: return
        val candidate = findExplicitTarget(root) ?: return
        val clickable = findClickableAncestor(candidate) ?: candidate
        if (clickable.isClickable && clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            lastActionAt = now
            val prefs = getSharedPreferences("qingping_stats", MODE_PRIVATE)
            prefs.edit().putInt("total", prefs.getInt("total", 0) + 1).apply()
        }
    }

    private fun findExplicitTarget(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val label = (node.text?.toString() ?: "") + " " + (node.contentDescription?.toString() ?: "")
        if (targets.any { label.trim() == it }) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findExplicitTarget(child)
            if (found != null) return found
        }
        return null
    }

    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(5) {
            if (current?.isClickable == true) return current
            current = current?.parent
        }
        return null
    }

    override fun onInterrupt() = Unit
}
