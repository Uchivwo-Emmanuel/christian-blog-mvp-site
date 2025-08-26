package org.example.rabbi.service


import org.example.rabbi.model.CategoryDTO
import org.example.rabbi.model.PostDTO
import org.example.rabbi.model.WebPostDTO
import org.example.rabbi.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate

@Service
class WebAppService(private val categoryRepository: CategoryRepository) {


    fun saveFileToUploadFolder(image: MultipartFile): String? {
        val uploadDir = Paths.get("uploads")
        if (!Files.exists(uploadDir)) {
            Files.createDirectory(uploadDir)
        }
        println("Category POST endpoint hit")
        val imageFileName = image.originalFilename
        val imagePath = image.originalFilename?.let { uploadDir.resolve(it) }
        image.transferTo(imagePath!!)

        return imageFileName
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    fun categoriesToDTO(numberOfTakes: Int): List<CategoryDTO> {
        val categories = categoryRepository.findAllWithWebPosts()

        return categories.map { category ->
            // Safely map and limit web posts
            val limitedPosts = category.webPosts
                .map { post ->
                    WebPostDTO(
                        id = post.id ?: 0,
                        title = post.title ?: "No Title",
                        introduction = post.introduction,
                        categoryName = category.title ?: "Uncategorized",
                        titleImageName = post.titleImageName,
                        createdOn = post.createdOn?.toString(),
                        points = emptyList() // Or map if needed
                    )
                }
                .take(numberOfTakes) // ✅ Limit to numberOfTakes

            CategoryDTO(
                id = category.id ?: 0,
                title = category.title ?: "No Title",
                imageUrl = category.imageName,
                description = category.description,
                webPosts = limitedPosts
            )
        }
    }

    fun getAllPostsSorted(limit: Int): List<PostDTO> {
        val categories = categoryRepository.findAllWithWebPosts()
        val allPosts = categories.flatMap { category ->
            category.webPosts.mapNotNull { post ->
                // Skip posts without title or intro
                val title = post.title ?: return@mapNotNull null
                val introduction = post.introduction?.trim()

                val excerpt = when {
                    introduction.isNullOrBlank() -> "Click to read more."
                    introduction.length <= 120 -> introduction
                    else -> introduction.take(100) + "..."
                }

                category.title?.let {
                    PostDTO(
                        id = post.id!!,
                        title = title,
                        excerpt = excerpt,
                        imageUrl = post.titleImageName ?: "default-post.jpg", // fallback
                        categoryName = it,
                        categoryColor = getCategoryColor(category.title!!),
                        publishDate = post.createdOn?.toLocalDate() ?: LocalDate.now(),
                        authorName = null
                    )
                }
            }
        }
        return allPosts
            .sortedByDescending { it.publishDate }
            .take(limit)
    }

    // Optional: Assign colors to categories
    private fun getCategoryColor(categoryTitle: String): String {
        return when (categoryTitle.lowercase().trim()) {
            "doctrine" -> "#1abc9c"                 // Turquoise
            "testimonies" -> "#9b59b6"              // Amethyst
            "school of the spirit" -> "#3498db"     // Peter
            "prayer" -> "#e74c3c"                   // Alizarin
            "worship" -> "#f39c12"                  // Orange
            else -> "#7f8c8d"                       // Gray
        }
    }

    // Directory where images are stored
    private val uploadDir = "/uploads/"

    /**
     * Deletes a file by filename from the upload directory.
     * @param filename The name of the file to delete (e.g., "cat_123.jpg")
     * @return true if deleted, false if failed or didn't exist
     */
    fun deleteImage(filename: String?): Boolean {
        // Guard: null or empty filename
        if (filename.isNullOrBlank()) return false

        try {
            // Build path: uploadDir + filename
            val filePath: Path = Paths.get(uploadDir, filename)

            // Check if file exists
            if (!Files.exists(filePath)) {
                println("File not found: $filePath")
                return false
            }

            // Delete the file
            Files.delete(filePath)
            println("✅ Successfully deleted: $filePath")
            return true

        } catch (e: Exception) {
            println("❌ Failed to delete image $filename: ${e.message}")
            return false
        }
    }
}