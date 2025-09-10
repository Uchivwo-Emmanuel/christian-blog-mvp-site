package org.example.rabbi.repository

import org.example.rabbi.model.WebPost
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WebPostRepository: JpaRepository<WebPost, Long?> {

    @EntityGraph(attributePaths = ["points", "category", "appUser"])
    fun findWithPointById(id: Long): WebPost?

    @EntityGraph(attributePaths = ["points", "category", "appUser"])
    override fun findAll(): List<WebPost>

    fun findFirstByOrderByUpdatedOnDesc(): WebPost?

}