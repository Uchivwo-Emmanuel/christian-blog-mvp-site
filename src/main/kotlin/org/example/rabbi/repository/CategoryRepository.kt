package org.example.rabbi.repository

import org.example.rabbi.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CategoryRepository : JpaRepository<Category, Long?> {
    fun findCategoryByTitle(name: String): Category?
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.webPosts")
    fun findAllWithWebPosts(): List<Category>

}