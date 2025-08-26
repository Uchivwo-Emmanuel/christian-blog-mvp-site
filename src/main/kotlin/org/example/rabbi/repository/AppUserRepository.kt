package org.example.rabbi.repository

import org.example.rabbi.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppUserRepository : JpaRepository<AppUser, Long> {
    fun findByAppUserEmail(email: String): AppUser?

    // ✅ Add this method:
    fun existsByRole(role: String): Boolean
}