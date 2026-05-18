package com.example.parental_watch.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val activeOverlays = mutableListOf<View>()
    private val TAG = "ParentalWatch_Overlay"

    fun showOverlay(bounds: Rect) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return

        val overlayView = View(context).apply {
            // PAKAI MERAH TERANG AGAR TERLIHAT SAAT TESTING
            setBackgroundColor(Color.RED)
            alpha = 0.7f
        }

        // Konfigurasi Window agar presisi di seluruh layar (Full Screen)
        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }

        try {
            windowManager.addView(overlayView, params)
            activeOverlays.add(overlayView)
            Log.d(TAG, "✓ Overlay muncul di: x=${bounds.left}, y=${bounds.top}")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Gagal addView: ${e.message}")
        }
    }

    fun removeAllOverlays() {
        val views = ArrayList(activeOverlays)
        activeOverlays.clear()
        views.forEach { 
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
    }
}