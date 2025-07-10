package org.example.rabbi.repository

import org.example.rabbi.model.PostPoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostPointRepository: JpaRepository<PostPoint, Long?> {
}