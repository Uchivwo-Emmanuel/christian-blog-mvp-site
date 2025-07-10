package org.example.rabbi.repository

import org.example.rabbi.model.WebPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WebPostRepository: JpaRepository<WebPost, Long?> {
}