package org.example.rabbi.model

data class CategoryDTO (
    var id: Long? = null,
    var title: String?,
    var imageUrl: String? = null,
    var description: String? = null,
    var webPosts: List<WebPostDTO> = mutableListOf(),
    var appUser: AppUser? = null
)