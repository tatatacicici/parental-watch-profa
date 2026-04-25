package com.example.parental_watch.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class ParentalDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device Admin berhasil diaktifkan
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // Device Admin dinonaktifkan — app sekarang bisa diuninstall
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Menonaktifkan perlindungan akan memungkinkan aplikasi dihapus. " +
                "Masukkan PIN orang tua untuk melanjutkan."
    }
}