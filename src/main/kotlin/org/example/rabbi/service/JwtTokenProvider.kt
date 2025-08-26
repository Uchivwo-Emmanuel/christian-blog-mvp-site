// src/main/kotlin/org/example/rabbi/service/JwtTokenProvider.kt

package org.example.rabbi.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtTokenProvider(
    @Value("\${JWT_SECRET}") private val secret: String
) {
    private val expirationTime: Long = 86400000 // 24 hours
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    /**
     * Generate JWT token from UserDetails
     */
    fun createToken(userDetails: org.springframework.security.core.userdetails.UserDetails): String {
        val currentDate = Date()
        val expiryDate = Date(currentDate.time + expirationTime)

        return JWT.create()
            .withSubject(userDetails.username)  // Email as subject
            .withClaim("roles", userDetails.authorities.map { it.authority }) // Include roles
            .withIssuedAt(currentDate)
            .withExpiresAt(expiryDate)
            .sign(algorithm)
    }

    /**
     * Validate the JWT token
     */
    fun validateToken(token: String): Boolean {
        return try {
            JWT.require(algorithm).build().verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extract email (subject) from JWT token
     */
    fun extractEmail(token: String): String? {
        return try {
            val jwt = JWT.require(algorithm).build().verify(token)
            jwt.subject // subject is the email
        } catch (e: TokenExpiredException) {
            println("Token has expired: ${e.message}")
            null
        } catch (e: Exception) {
            println("Invalid token: ${e.message}")
            null
        }
    }

    /**
     * Extract claims (e.g., roles) from token
     */
    fun getClaim(token: String, claimName: String): String? {
        return try {
            val jwt = JWT.require(algorithm).build().verify(token)
            jwt.getClaim(claimName).asString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract roles from token
     */
    fun getRoles(token: String): List<String> {
        return try {
            val jwt = JWT.require(algorithm).build().verify(token)
            jwt.getClaim("roles").asList(String::class.java) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}