package com.example.parental_watch.network

import retrofit2.http.Body
import retrofit2.http.POST

data class ClassifyRequest(val text: String)

data class ClassifyResponse(
    val label: String,
    val confidence: Float,
    val is_offensive: Boolean
)

interface ApiService {
    @POST("classify")
    suspend fun classify(@Body request: ClassifyRequest): ClassifyResponse
}