package com.example.smartcontactupdater.detector

class TitleDetector {

    private val titleKeywords = mapOf(
        "doctor" to listOf("doctor", "dr", "dr."),
        "engineer" to listOf("engineer", "eng", "eng."),
        "professor" to listOf("professor", "prof", "prof."),
        "mr" to listOf("mr", "mr.", "mister"),
        "mrs" to listOf("mrs", "mrs.", "misses"),
        "miss" to listOf("miss", "ms", "ms.")
    )

    fun detectTitle(transcript: String): String? {
        if (transcript.isBlank()) return null
        
        val lowerTranscript = transcript.lowercase()
        val words = lowerTranscript.split(Regex("\\s+"))
        
        for (word in words) {
            for ((title, keywords) in titleKeywords) {
                if (keywords.any { it == word }) {
                    return formatTitle(title)
                }
            }
        }
        
        return null
    }

    private fun formatTitle(title: String): String {
        return when (title) {
            "doctor" -> "Dr"
            "engineer" -> "Eng"
            "professor" -> "Prof"
            "mr" -> "Mr"
            "mrs" -> "Mrs"
            "miss" -> "Ms"
            else -> title.replaceFirstChar { it.uppercase() }
        }
    }
}
