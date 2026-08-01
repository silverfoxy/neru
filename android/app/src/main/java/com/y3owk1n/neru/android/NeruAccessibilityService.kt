package com.y3owk1n.neru.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class NeruAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private var overlay: GridOverlayView? = null
    private var navigator: GridNavigator? = null
    private var consumeActivationKeyUp = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        hideGrid()
    }

    override fun onDestroy() {
        hideGrid()
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (isActivationChord(event)) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                consumeActivationKeyUp = true
                toggleGrid()
            } else if (event.action == KeyEvent.ACTION_UP) {
                consumeActivationKeyUp = false
            }
            return true
        }

        if (consumeActivationKeyUp && event.keyCode == KeyEvent.KEYCODE_G) {
            if (event.action == KeyEvent.ACTION_UP) consumeActivationKeyUp = false
            return true
        }

        if (overlay == null) return false

        // Consume both halves of every grid command to keep the stream consistent.
        if (event.action == KeyEvent.ACTION_UP) return isGridCommand(event)
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return true

        return when (event.keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> {
                hideGrid()
                true
            }
            KeyEvent.KEYCODE_DEL -> {
                navigator?.back()
                updateOverlay()
                true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                tapSelection()
                true
            }
            else -> handleSelectionKey(event)
        }
    }

    private fun isActivationChord(event: KeyEvent): Boolean =
        event.keyCode == KeyEvent.KEYCODE_G && event.isCtrlPressed && event.isShiftPressed

    private fun isGridCommand(event: KeyEvent): Boolean =
        event.keyCode == KeyEvent.KEYCODE_ESCAPE ||
            event.keyCode == KeyEvent.KEYCODE_DEL ||
            event.keyCode == KeyEvent.KEYCODE_ENTER ||
            event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
            gridKey(event.keyCode) != null

    private fun handleSelectionKey(event: KeyEvent): Boolean {
        val key = gridKey(event.keyCode) ?: return false
        navigator?.accept(key)
        updateOverlay()
        return true
    }

    private fun gridKey(keyCode: Int): Char? = when (keyCode) {
        KeyEvent.KEYCODE_A -> 'a'
        KeyEvent.KEYCODE_S -> 's'
        KeyEvent.KEYCODE_D -> 'd'
        KeyEvent.KEYCODE_F -> 'f'
        KeyEvent.KEYCODE_G -> 'g'
        KeyEvent.KEYCODE_H -> 'h'
        KeyEvent.KEYCODE_J -> 'j'
        KeyEvent.KEYCODE_K -> 'k'
        KeyEvent.KEYCODE_L -> 'l'
        else -> null
    }

    private fun toggleGrid() {
        if (overlay == null) showGrid() else hideGrid()
    }

    private fun showGrid() {
        val bounds = if (android.os.Build.VERSION.SDK_INT >= 30) {
            windowManager.currentWindowMetrics.bounds
        } else {
            @Suppress("DEPRECATION")
            android.graphics.Rect(0, 0, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
        }
        navigator = GridNavigator(RectF(bounds))
        overlay = GridOverlayView(this).also { view ->
            view.navigator = navigator!!
            windowManager.addView(view, overlayLayoutParams())
        }
    }

    private fun hideGrid() {
        overlay?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        overlay = null
        navigator = null
    }

    private fun updateOverlay() {
        overlay?.refresh()
    }

    private fun tapSelection() {
        val selected = navigator?.targetBounds ?: return
        val x = selected.centerX()
        val y = selected.centerY()
        hideGrid()

        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun overlayLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    companion object {
        const val ACTION_TOGGLE_GRID = "com.y3owk1n.neru.android.action.TOGGLE_GRID"

        @Volatile
        private var instance: NeruAccessibilityService? = null

        fun toggleFromExternalShortcut() {
            Handler(Looper.getMainLooper()).post { instance?.toggleGrid() }
        }
    }
}
