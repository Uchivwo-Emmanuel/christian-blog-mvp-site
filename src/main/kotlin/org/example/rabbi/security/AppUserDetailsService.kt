// src/main/kotlin/org/example/rabbi/security/AppUserDetailsService.kt

package org.example.rabbi.security

import org.example.rabbi.repository.AppUserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(
    private val appUserRepository: AppUserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = appUserRepository.findByAppUserEmail(email.lowercase())
            ?: throw UsernameNotFoundException("Admin not found: $email")

        return User(
            user.appUserEmail!!,
            user.password!!,
            listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
        )
    }
}