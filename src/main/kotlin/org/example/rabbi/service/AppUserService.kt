// src/main/kotlin/org/example/rabbi/service/AppUserService.kt

package org.example.rabbi.service

import org.example.rabbi.model.AppUser
import org.example.rabbi.repository.AppUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Register a new admin user.
     * Only ADMIN role is allowed.
     */
    @Transactional
    fun registerUser(appUser: AppUser): AppUser {
        // Validate role
        if (appUser.role != "ADMIN") {
            throw IllegalArgumentException("Only ADMIN role is permitted.")
        }

        // Normalize email
        val normalizedEmail = appUser.appUserEmail?.lowercase()?.trim()

        // Check if email already exists
        if (appUserRepository.findByAppUserEmail(normalizedEmail!!) != null) {
            throw IllegalArgumentException("Email already in use: $normalizedEmail")
        }

        // Hash password
        appUser.password = passwordEncoder.encode(appUser.password)

        // Set normalized email
        appUser.appUserEmail = normalizedEmail

        return appUserRepository.save(appUser)
    }

    /**
     * Login: Validate credentials and return AppUser if valid.
     * Does NOT return JWT — handled by AuthService.
     */
    fun loginUser(email: String, password: String): AppUser? {
        val normalizedEmail = email.lowercase().trim()
        val user = appUserRepository.findByAppUserEmail(normalizedEmail) ?: return null

        // Validate password
        if (!passwordEncoder.matches(password, user.password)) return null

        // Validate role
        if (user.role != "ADMIN") return null

        return user
    }

    /**
     * Find user by ID
     */
    fun findById(id: Long): AppUser? {
        return appUserRepository.findById(id).orElse(null)
    }

    /**
     * Update admin details
     */
    @Transactional
    fun updateAdmin(
        id: Long,
        firstName: String,
        lastName: String,
        email: String,
        password: String?,
        adminImage: String?
    ): AppUser? {
        val user = appUserRepository.findById(id).orElse(null) ?: return null

        user.firstName = firstName.trim()
        user.lastName = lastName.trim()
        user.appUserEmail = email.lowercase().trim()

        if (!password.isNullOrEmpty()) {
            user.password = passwordEncoder.encode(password)
        }

        if (adminImage != null) {
            user.adminImage = adminImage
        }

        return appUserRepository.save(user)
    }

    /**
     * Delete admin by ID
     */
    @Transactional
    fun deleteAdmin(id: Long): Boolean {
        if (appUserRepository.existsById(id)) {
            appUserRepository.deleteById(id)
            return true
        }
        return false
    }

    /**
     * Implement UserDetailsService for Spring Security
     */
    /*override fun loadUserByUsername(email: String): UserDetails {
        val normalizedEmail = email.lowercase().trim()
        val user = appUserRepository.findByAppUserEmail(normalizedEmail)
            ?: throw UsernameNotFoundException("Admin not found: $normalizedEmail")

        return org.springframework.security.core.userdetails.User
            .withUsername(user.appUserEmail!!)
            .password(user.password!!)
            .authorities(listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${user.role}")))
            .build()
    }*/
}