package org.example.rabbi.controller

import org.example.rabbi.model.Category
import org.springframework.ui.Model
import org.example.rabbi.repository.CategoryRepository
import org.example.rabbi.repository.WebPostRepository
import org.example.rabbi.service.WebAppService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/post")
class GeneralController(
    private val categoryRepository: CategoryRepository,
    private val webAppService: WebAppService,
    private val webPostRepository: WebPostRepository
) {
    @PostMapping("/categories")
    fun createCategory(
        @RequestParam title: String,
        @RequestParam description: String,
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<out Any> {
        //save image
        val imageFileName = webAppService.saveFileToUploadFolder(image)
        val category = Category(
            id = null,
            title = title,
            description = description,
            imageName = imageFileName
        )
        val savedCategory = categoryRepository.save(category)

        return ResponseEntity.ok(mapOf(
            "saved Category" to savedCategory
        ))
    }

    @GetMapping("/create-category")
    fun showCategoryForm(): String {
        return "create-category" // This loads create-category.html
    }
    @GetMapping("/make-post")
    fun showPostForm(): String {
        return "post-creation" // This loads create-category.html
    }

    @GetMapping("/view-post")
    fun showPostPage(@RequestParam id: Long, model: Model): String {
        val post = webPostRepository.findById(id).orElseThrow()
        model.addAttribute("post", post)
        return "post"
    }

    @GetMapping("/")
    fun showCategories(model: Model): String {
        model.addAttribute("categories", webAppService.categoriesToDTO(3))
        return "index" // This should match your HTML file name in templates (e.g., categories.html)
    }

    @GetMapping("/category/{id}")
    fun getCategoryPage(@PathVariable id: Long, model: Model): String {
        val category = categoryRepository.findById(id).orElseThrow { RuntimeException("Category with $id was not found in repository")}
        model.addAttribute("category", category)
        model.addAttribute("posts", category.webPosts)
        return "category-page"
    }

}