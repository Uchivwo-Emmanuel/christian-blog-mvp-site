package org.example.rabbi.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@Service
class PostService {

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
}