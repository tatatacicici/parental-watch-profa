package com.example.parental_watch.utils

/**
 * DummyClassifier — simulasi model NLP untuk keperluan prototype.
 *
 * Cara kerja dummy ini:
 * 1. Cek daftar kata kasar hardcoded (untuk demo yang lebih realistis)
 * 2. Kalau tidak ada di daftar, random 20% chance dianggap kasar
 *
 * Nanti diganti dengan RealClassifier yang kirim request ke server FastAPI.
 */
class DummyClassifier {

    data class ClassificationResult(
        val text: String,
        val isOffensive: Boolean,
        val confidence: Float,
        val label: String
    )

    // Daftar kata kasar untuk demo — akan diganti model asli nanti
    private val offensiveKeywords = setOf(
        "anjing", "babi", "bangsat", "bajingan", "kontol",
        "memek", "tolol", "bodoh", "goblok", "kampret",
        "asu", "jancok", "cok", "ngentot", "tai"
    )

    fun classify(text: String): ClassificationResult {
        val lowerText = text.lowercase()

        // Cek kata kasar hardcoded
        val containsOffensive = offensiveKeywords.any { keyword ->
            lowerText.contains(keyword)
        }

        return if (containsOffensive) {
            ClassificationResult(
                text = text,
                isOffensive = true,
                confidence = 0.95f,
                label = "offensive"
            )
        } else {
            // Simulasi: 20% chance false positive untuk testing overlay
            val randomOffensive = Math.random() < 0.05 // 5% random
            ClassificationResult(
                text = text,
                isOffensive = randomOffensive,
                confidence = if (randomOffensive) 0.75f else 0.90f,
                label = if (randomOffensive) "offensive" else "clean"
            )
        }
    }
}