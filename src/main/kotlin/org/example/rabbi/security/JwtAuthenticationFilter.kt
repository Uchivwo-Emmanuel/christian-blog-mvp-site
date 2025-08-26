// src/main/kotlin/org/example/rabbi/security/JwtAuthenticationFilter.kt

package org.example.rabbi.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.rabbi.service.JwtTokenProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        println("🔐 AUTH HEADER: $authHeader") // ← Check this

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            println("🔐 TOKEN: $token") // ← Is it correct?

            try {
                val email = jwtTokenProvider.extractEmail(token)
                println("📧 EMAIL FROM TOKEN: $email") // ← Should print

                val userDetails = userDetailsService.loadUserByUsername(email)
                println("👤 USER DETAILS LOADED: ${userDetails.username}") // ← Must print

                val auth = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = auth
                println("✅ AUTHENTICATION SET") // ← Critical: must see this
            } catch (e: Exception) {
                println("❌ AUTH FAILED: ${e.javaClass.simpleName} - ${e.message}")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                return
            }
        } else {
            println("🚫 NO AUTH HEADER OR NOT BEARER")
        }

        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}