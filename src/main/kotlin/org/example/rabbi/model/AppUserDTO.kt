package org.example.rabbi.model

data class AppUserDTO(
    val id: Long?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    var adminImage: String?,
    val role: String?
)
