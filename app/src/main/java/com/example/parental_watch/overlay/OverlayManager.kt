package com.example.parental_watch.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo

/**
 * OverlayManager — menggambar kotak hitam di atas teks yang terdeteksi kasar.
 *
 * Cara kerja:
 * 1. Ambil koordinat node (posisi teks di layar)
 * 2. Buat View kotak hitam dengan ukuran yang sama
 * 3. Tampilkan via WindowManager di atas semua aplikasi
 *
 * Butuh permission SYSTEM_ALERT_WINDOW yang di-grant manual oleh user.
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val activeOverlays = mutableListOf<View>()

    fun showOverlay(node: AccessibilityNodeInfo){
        //take node on screen
        val bounds =  Rect()
        node.getBoundsInScreen(bounds)

        if(bounds.width() <= 0 || bounds.height() <= 0) return

        val overlayView = View(context).apply {
            setBackgroundColor(Color.BLACK)
            tag = node.hashCode()
        }

        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            bounds.left,
            bounds.top,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try{
            windowManager.addView(overlayView, params)
            activeOverlays.add(overlayView)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun removeAllOverlays(){
        for(view in activeOverlays){
            try{
                windowManager.removeView(view)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        activeOverlays.clear()
    }

    fun refreshOverlays(){
        // Dipanggil saat user scroll — hapus overlay lama
        // Service akan regenerate overlay untuk posisi baru
        removeAllOverlays()
    }

}
