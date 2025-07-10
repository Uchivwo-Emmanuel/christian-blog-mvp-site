package org.example.rabbi.repository

import org.example.rabbi.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, Long?> {
    fun findCategoryByTitle(name: String): Category?
}