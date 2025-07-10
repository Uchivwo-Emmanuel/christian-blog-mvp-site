package org.example.rabbi.controller

import org.example.rabbi.model.CategoryDTO
import org.example.rabbi.model.PostPoint
import org.example.rabbi.model.WebPost
import org.springframework.web.bind.annotation.*

import org.example.rabbi.repository.CategoryRepository
import org.example.rabbi.repository.PostPointRepository
import org.example.rabbi.repository.WebPostRepository
import org.example.rabbi.service.PostService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest


@RestController
@RequestMapping("/api")
class ControllerForRestApi(
    private val categoryRepository: CategoryRepository,
    private val webPostRepository: WebPostRepository,
    private val postService: PostService,
    private val postPointRepository: PostPointRepository,
    private val generalController: GeneralController
) {

    @GetMapping("/get-categories")
    fun getCategories(): ResponseEntity<out Any> {
        val categories = categoryRepository.findAll()
        val categoryDTOs = generalController.categoriesToDTO(categories,categories.size)
        return ResponseEntity.status(HttpStatus.OK).body(mapOf(
            "categories" to categoryDTOs
        ))
    }

    @PostMapping("/create-post", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPost(@RequestParam title: String,
                   @RequestParam introduction: String,
                   @RequestParam("titleImage") titleImage: MultipartFile,
                   @RequestParam categoryName: String,
                   request: MultipartHttpServletRequest): ResponseEntity<out Any> {
        val category = categoryRepository.findCategoryByTitle(categoryName)
        val points = mutableListOf<PostPoint>()
        var index = 0
        while (true) {
            val pointTitle = request.getParameter("points[$index].pointTitle") ?: break
            val pointBody = request.getParameter("points[$index].pointBody")
            val pointImage = request.getFile("points[$index].pointImage")

            //Add to points mutableListOf<PostPoint>()
            val postPoint = PostPoint(
                id = null,
                pointHeading = pointTitle,
                pointBody = pointBody,
                pointImageName = pointImage?.let { postService.saveFileToUploadFolder(it) },
                post = null
            )
            points.add(postPoint)
            index++
        }
        println(points.joinToString { " , " })
        //save parameters to web post
        val newPost = WebPost(
            id = null,
            title = title,
            titleImageName = postService.saveFileToUploadFolder(titleImage),
            introduction = introduction,
            category = category,
            points = points,
        )
        points.forEach { it.post = newPost }
        /*postPointRepository.saveAll(points)*///Hibernate will cascade the save of points when you save WebPost.
        newPost.points = points
        webPostRepository.save(newPost)// saves everything in one go
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "post" to newPost
        ))
    }

    @GetMapping("/post/{id}")
    fun getPostById(@PathVariable id: Long): ResponseEntity<out Any> {
        val post = webPostRepository.findById(id).orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post Id does not exist")
        return ResponseEntity.status(HttpStatus.OK).body(mapOf("post" to post))
    }
}