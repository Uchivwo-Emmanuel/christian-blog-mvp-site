package org.example.rabbi.service


import org.example.rabbi.model.CategoryDTO
import org.example.rabbi.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

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
        val categoryDTOs = categories.map {category ->
            val limitedPosts = category.webPosts.take(numberOfTakes)
            CategoryDTO(
                id = category.id,
                title = category.title,
                imageUrl = category.imageName,
                webPosts = limitedPosts
            )
        }
        return categoryDTOs
    }
}