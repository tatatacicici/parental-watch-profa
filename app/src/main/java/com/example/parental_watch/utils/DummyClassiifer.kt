package com.example.parental_watch.utils

class DummyClassifier {

    data class ClassificationResult(
        val text: String,
        val isOffensive: Boolean,
        val confidence: Float,
        val label: String
    )

    private val offensiveKeywords = setOf(
        "anjing", "babi", "bangsat", "bajingan", "kontol",
        "memek", "tolol", "bodoh", "goblok", "kampret",
        "asu", "jancok", "cok", "ngentot", "tai", "sialan",
        "keparat", "brengsek", "kurang ajar", "setan"
    )

    fun classify(text: String): ClassificationResult {
        val lowerText = text.lowercase()

        val matchedKeyword = offensiveKeywords.firstOrNull { keyword ->
            lowerText.contains(keyword)
        }

        return if (matchedKeyword != null) {
            ClassificationResult(
                text = text,
                isOffensive = true,
                confidence = 0.95f,
                label = "offensive"
            )
        } else {
            ClassificationResult(
                text = text,
                isOffensive = false,
                confidence = 0.90f,
                label = "clean"
            )
        }
    }
}