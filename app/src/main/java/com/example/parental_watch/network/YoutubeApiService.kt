package com.example.parental_watch.network

import retrofit2.http.GET
import retrofit2.http.Query

// YouTube Data API v3 models
data class YoutubeSearchResponse(
    val items: List<YoutubeSearchItem>
)

data class YoutubeSearchItem(
    val id: YoutubeVideoId,
    val snippet: YoutubeSnippet
)

data class YoutubeVideoId(
    val videoId: String?
)

data class YoutubeSnippet(
    val title: String,
    val description: String,
    val channelTitle: String,
    val thumbnails: YoutubeThumbnails
)

data class YoutubeThumbnails(
    val medium: YoutubeThumbnail?
)

data class YoutubeThumbnail(
    val url: String
)

data class YoutubeCommentResponse(
    val items: List<YoutubeCommentItem>
)

data class YoutubeCommentItem(
    val snippet: YoutubeCommentSnippet
)

data class YoutubeCommentSnippet(
    val topLevelComment: YoutubeTopLevelComment
)

data class YoutubeTopLevelComment(
    val snippet: YoutubeCommentTextSnippet
)

data class YoutubeCommentTextSnippet(
    val textDisplay: String,
    val likeCount: Int = 0
)

interface YoutubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 10,
        @Query("safeSearch") safeSearch: String = "strict",
        @Query("key") apiKey: String
    ): YoutubeSearchResponse

    @GET("commentThreads")
    suspend fun getTopComments(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("order") order: String = "relevance",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YoutubeCommentResponse

    @GET("commentThreads")
    suspend fun getRecentComments(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("order") order: String = "time",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YoutubeCommentResponse
}
