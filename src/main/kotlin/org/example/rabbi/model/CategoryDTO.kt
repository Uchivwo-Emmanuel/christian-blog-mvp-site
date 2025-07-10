package org.example.rabbi.model

data class CategoryDTO (
    var id: Long? = null,
    var title: String?,
    var imageUrl: String? = null,
    var webPosts: List<WebPost> = mutableListOf()
)