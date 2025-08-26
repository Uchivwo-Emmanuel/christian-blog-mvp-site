package org.example.rabbi.model

import java.time.LocalDate

data class PostDTO(
    val id: Long,
    val title: String,
    val excerpt: String,           // Shortened introduction
    val imageUrl: String,          // From titleImageName
    val categoryName: String,      // From linked Category
    val categoryColor: String,     // Optional: color based on category
    val publishDate: LocalDate,    // From createdOn (only date part)
    val authorName: String?        // Optional: author from appUser
)