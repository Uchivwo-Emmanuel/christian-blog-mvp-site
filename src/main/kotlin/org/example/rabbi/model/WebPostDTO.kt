package org.example.rabbi.model

import java.time.LocalDateTime
// WebPostDTO.kt
data class WebPostDTO(
    val id: Long,
    val title: String,
    val introduction: String?,
    val categoryName: String, // ← Flat string, never null
    val titleImageName: String?,
    val createdOn: String?, // Format as string to avoid serialization issues
    val points: List<PostPointDTO> // Pre-serialized JSON for JS
)