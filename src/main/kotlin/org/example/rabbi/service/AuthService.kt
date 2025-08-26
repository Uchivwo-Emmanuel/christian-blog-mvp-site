package org.example.rabbi.service

import org.example.rabbi.security.AppUserDetailsService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val appUserService: AppUserService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val appUserDetailsService: AppUserDetailsService
) {

    fun login(email: String, password: String): Map<String, Any>? {
        val user = appUserService.loginUser(email, password) ?: return null
        val userDetails = appUserDetailsService.loadUserByUsername(user.appUserEmail!!)
        val token = jwtTokenProvider.createToken(userDetails)
        return mapOf(
            "token" to token,
            "user" to mapOf(
                "id" to user.appUserId,
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "email" to user.appUserEmail,
                "role" to user.role,
                "adminImage" to user.adminImage
            )
        )
    }
}