package com.example.parental_watch.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Ganti IP ini dengan IP laptop kamu di jaringan WiFi yang sama
    // Cek IP laptop: di terminal ketik `ip addr` (Linux) atau `ipconfig` (Windows)
    private const val BASE_URL = "http://192.168.1.100:8000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Fungsi untuk update BASE_URL secara dinamis
    // (jika IP server berubah, orang tua bisa update dari panel)
    fun buildWithUrl(url: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}