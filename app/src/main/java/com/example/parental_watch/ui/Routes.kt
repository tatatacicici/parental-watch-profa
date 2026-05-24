package com.example.parental_watch.ui

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val PERMISSION = "permission"
    const val PIN_SETUP = "pin_setup"
    const val PARENT_LOGIN = "parent_login"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val CHILD_MODE = "child_mode"
    const val WHITELIST = "whitelist"
    const val LOG = "log"
    const val CHANGE_PIN = "change_pin"
    const val FORGOT_PASSWORD = "forgot_password"

    // Fitur YouTube wrapper
    const val SEARCH = "search"
    const val VIDEO_CHECK = "video_check/{videoId}/{title}/{channelTitle}/{thumbnailUrl}"
    const val PLAYER = "player/{videoId}/{title}"
    const val BLOCK = "block/{videoId}/{title}/{reason}/{ratio}"
    const val VIDEO_HISTORY = "video_history"
    const val STUDY_SCHEDULE = "study_schedule"

    const val GOOGLE_LOGIN = "google_login"

    // Helper untuk build route dengan argument
    fun videoCheck(videoId: String, title: String, channelTitle: String, thumbnailUrl: String) =
        "video_check/$videoId/${encode(title)}/${encode(channelTitle)}/${encode(thumbnailUrl)}"

    fun player(videoId: String, title: String) =
        "player/$videoId/${encode(title)}"

    fun block(videoId: String, title: String, reason: String, ratio: Float) =
        "block/$videoId/${encode(title)}/${encode(reason)}/$ratio"

    private fun encode(value: String) =
        java.net.URLEncoder.encode(value, "UTF-8")
}
