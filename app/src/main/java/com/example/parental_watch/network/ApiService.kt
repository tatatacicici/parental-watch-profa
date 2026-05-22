package com.example.parental_watch.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class ClassifyRequest(val text: String)

data class ClassifyResponse(
    val label: String,
    val confidence: Float,
    @SerializedName("is_offensive")
    val isOffensive: Boolean
)

// Tambah ini untuk batch
data class BatchClassifyRequest(val texts: List<String>)

data class BatchClassifyResponse(
    val results: List<ClassifyResponse>
)

interface ApiService {
    @POST("classify")
    suspend fun classify(@Body request: ClassifyRequest): ClassifyResponse

    @POST("classify-batch")
    suspend fun classifyBatch(@Body request: BatchClassifyRequest): BatchClassifyResponse
}
