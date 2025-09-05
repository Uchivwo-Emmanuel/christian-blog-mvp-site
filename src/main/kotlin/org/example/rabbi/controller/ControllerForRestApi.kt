package org.example.rabbi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.example.rabbi.model.*
import org.example.rabbi.repository.AppUserRepository
import org.springframework.web.bind.annotation.*

import org.example.rabbi.repository.CategoryRepository
import org.example.rabbi.repository.PostPointRepository
import org.example.rabbi.repository.WebPostRepository
import org.example.rabbi.service.AppUserService
import org.example.rabbi.service.AuthService
import org.example.rabbi.service.WebAppService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest


@RestController
@RequestMapping("/api")
class ControllerForRestApi(
    private val categoryRepository: CategoryRepository,
    private val webPostRepository: WebPostRepository,
    private val webAppService: WebAppService,
    private val postPointRepository: PostPointRepository,
    private val appUserService: AppUserService,
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,  // ✅ Inject here
    private val authService: AuthService
) {


    @Transactional(readOnly = true)
    @GetMapping("/get-categories")
    fun getCategories(): ResponseEntity<out Any> {
        val categories = categoryRepository.findAllWithWebPosts()
        val categoryDTOs = categories.map {category ->
            CategoryDTO(
                id = category.id,
                title = category.title,
                imageUrl = category.imageName,
                description = category.description,
                webPosts = category.webPosts.map { post ->
                    WebPostDTO(
                        id = post.id ?: 0,
                        title = post.title ?: "No Title",
                        introduction = post.introduction,
                        categoryName = category.title ?: "Uncategorized",
                        titleImageName = post.titleImageName,
                        createdOn = post.createdOn?.toString(),
                        points = post.points.map { p ->
                            PostPointDTO(
                                pointTitle = p.pointHeading ?: "",
                                pointBody = p.pointBody ?: "",
                                pointImageName = p.pointImageName
                            )
                        }
                    )
                }
            )
        }
        return ResponseEntity.status(HttpStatus.OK).body(mapOf(
            "categories" to categoryDTOs
        ))
    }
    @PostMapping("/create-category")
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

    @GetMapping("/posts")
    fun getAllPosts(): ResponseEntity<out Any> {
        val objectMapper = ObjectMapper().registerKotlinModule()
        val posts = webPostRepository.findAll()
        val postDTOs = posts.map {post ->
            WebPostDTO(
                id = post.id!!,
                title = post.title!!,
                introduction = post.introduction,
                categoryName = post.category?.title!!,
                titleImageName = post.titleImageName,
                createdOn = post.createdOn.toString(), // Format as string to avoid serialization issues
                points = post.points.map { p ->
                    PostPointDTO(
                        pointTitle = p.pointHeading ?: "",
                        pointBody = p.pointBody ?: "",
                        pointImageName = p.pointImageName
                    )
                }
            )
        }
        return ResponseEntity.status(HttpStatus.OK).body(mapOf(
            "posts" to postDTOs
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
                pointImageName = pointImage?.let { webAppService.saveFileToUploadFolder(it) },
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
            titleImageName = webAppService.saveFileToUploadFolder(titleImage),
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

    @PostMapping("/update-category")
    fun updateCategory(
        @RequestParam id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) description: String?,
        @RequestParam("image", required = false) image: MultipartFile?
    ): ResponseEntity<*> {
        val category = categoryRepository.findById(id).orElseThrow { Exception("Not found") }
            ?: return ResponseEntity.status(404).body("Admin not found")

        category.title = title.trim()
        category.description = description?.trim()

        if (image != null && !image.isEmpty) {
            // Delete old image if exists
            webAppService.deleteImage(category.imageName)
            category.imageName = webAppService.saveFileToUploadFolder(image)
        }

        categoryRepository.save(category)
        return ResponseEntity.ok().body(mapOf("message" to "Updated"))
    }

    @GetMapping("/categories/{id}")
    fun getCategory(@PathVariable id: Long): ResponseEntity<out Any> {
        // Find category with eager loading (prevents LazyInitializationException)
        val category = categoryRepository.findByIdWithPosts(id)
            ?: return ResponseEntity.notFound().build()

        // Map to simple DTO (safe for JSON)
        return ResponseEntity.ok(mapOf(
            "id" to category.id,
            "title" to category.title,
            "description" to category.description,
            "imageName" to category.imageName
        ))
    }


    @DeleteMapping("/delete-category/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<out Any> {
        val category = categoryRepository.findById(id).orElseThrow { Exception("Not found") }
        webAppService.deleteImage(category.imageName)
        categoryRepository.delete(category)
        return ResponseEntity.ok().body(mapOf("message" to "Deleted"))
    }


    @GetMapping("/posts/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<out Any> {
        val objectMapper = ObjectMapper().registerKotlinModule()
        val post = webPostRepository.findWithPointById(id) ?: return ResponseEntity.notFound().build()

        try {
            val dto = WebPostDTO(
                id = post.id ?: 0,
                title = post.title ?: "No Title",
                introduction = post.introduction ?: "",
                categoryName = post.category?.title ?: "Uncategorized",
                titleImageName = post.titleImageName,
                createdOn = post.createdOn.toString(),
                points = (post.points.map { p ->
                    PostPointDTO(
                        pointTitle = p.pointHeading ?: "",
                        pointBody = p.pointBody ?: "",
                        pointImageName = p.pointImageName
                    )
                }
                        )
            )

            return ResponseEntity.ok(dto)

        } catch (e: Exception) {
            return ResponseEntity.status(500).body("Error creating post DTO: ${e.message}")
        }
    }

    // Directory to save uploaded images
    val uploadDir = "/uploads"

    /**
     * Update an existing post
     */
    @PostMapping("/update-post")
    fun updatePost(
        @RequestParam id: Long,
        @RequestParam title: String,
        @RequestParam introduction: String,
        @RequestParam categoryName: String,
        @RequestParam("titleImage", required = false) titleImage: MultipartFile?,
        request: MultipartHttpServletRequest
    ): ResponseEntity<out Any> {

        // 1. Find the existing post
        val post = webPostRepository.findWithPointById(id)?: return ResponseEntity.notFound().build()

        // 2. Find the category
        val category = categoryRepository.findCategoryByTitle(categoryName)
            ?: return ResponseEntity.badRequest().body("Category not found")

        // 3. Update basic fields
        post.title = title.trim()
        post.introduction = introduction.trim()
        post.category = category

        // 4. Handle title image update
        if (titleImage != null && !titleImage.isEmpty) {
            // Delete old image if exists
            post.titleImageName?.let { webAppService.deleteImage(it) }
            // Save new image
            post.titleImageName = webAppService.saveFileToUploadFolder(titleImage)
        }

        // 5. Rebuild points from form data
        val updatedPoints = mutableListOf<PostPoint>()
        var index = 0
        while (true) {
            val pointTitle = request.getParameter("points[$index].pointTitle") ?: break
            val pointBody = request.getParameter("points[$index].pointBody")
            val pointImage = request.getFile("points[$index].pointImage")

            // Save new point image if provided
            val pointImageName = if (pointImage != null && !pointImage.isEmpty) {
                webAppService.saveFileToUploadFolder(pointImage)
            } else {
                null
            }

            // Reuse existing point if possible, otherwise create new
            val point = if (index < post.points.size) {
                val existing = post.points[index]
                existing.pointHeading = pointTitle
                existing.pointBody = pointBody

                // If new image, delete old one and update
                if (pointImageName != null && existing.pointImageName != null) {
                    webAppService.deleteImage(existing.pointImageName)
                }
                existing.pointImageName = pointImageName

                existing
            } else {
                PostPoint(
                    id = null,
                    pointHeading = pointTitle,
                    pointBody = pointBody,
                    pointImageName = pointImageName,
                    post = post
                )
            }

            updatedPoints.add(point)
            index++
        }

        // 6. Remove any old points not in the updated list
        val pointsToRemove = post.points.filter { it !in updatedPoints }
        pointsToRemove.forEach { point ->
            point.pointImageName?.let { webAppService.deleteImage(it) }
        }

        // Clear and update points list
        post.points.clear()
        post.points.addAll(updatedPoints)

        // 7. Save post (cascade saves points)
        return try {
            webPostRepository.save(post)
            ResponseEntity.ok().body(mapOf("message" to "Post updated successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Error saving post: ${e.message}")
        }
    }

    @Transactional
    @DeleteMapping("/delete-post/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<out Any> {
        // 1. Find the post
        val post = webPostRepository.findWithPointById(id)?: return ResponseEntity.notFound().build()

        println("this is the post to be deleted $post")
        // 2. Delete title image if exists
        post.titleImageName?.let { webAppService.deleteImage(it) }

        // 3. Delete all point images
        post.points.forEach { point ->
            point.pointImageName?.let { webAppService.deleteImage(it) }
        }

        // 4. Delete the post (points cascade due to CascadeType.ALL and orphanRemoval)
        return try {
            webPostRepository.delete(post)
            webPostRepository.flush() // Force sync with DB
            println("post successfully deleted")
            ResponseEntity.ok().body(mapOf("message" to "Post deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Error deleting post: ${e.message}")
        }
    }

//    @PostMapping("/login")
//    fun login(@RequestParam email: String, @RequestParam password: String,
//              request: HttpServletRequest
//    ) : ResponseEntity<out Any>{
//        val token = appUserService.loginUser(email, password)
//        return if(token != null){
////            val csrfToken = request.getAttribute(CsrfToken::class.java.name) as? CsrfToken
////            /*val csrfTokenValue = csrfToken?.tokenEmail(email)
//
////            roomBookingService.checkAndReleaseRooms(?: ""*/
//            val user = appUserRepository.findByAppUserEmail(email)
//            val userDTO = user?.let { toAppUserDTO(it) }
//            ResponseEntity.ok(mapOf("token" to token,
//                "user" to userDTO))
//        }else{
//            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Email or Password")
//        }
//    }

    @GetMapping("/admins")
    fun getAdmins(): ResponseEntity<out Any> {
        val admins = appUserRepository.findAll().map {
            mapOf(
                "id" to it.appUserId,
                "firstName" to it.firstName,
                "lastName" to it.lastName,
                "email" to it.appUserEmail,
                "adminImage" to it.adminImage,
                "createdOn" to it.createdOn
            )
        }
        return ResponseEntity.ok(mapOf("admins" to admins))
    }

    // ControllerForRestApi.kt
    @GetMapping("/validate-token")
    fun validateToken(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<String> {
        return if (userDetails != null) {
            ResponseEntity.ok("Valid")
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token")
        }
    }

    @PostMapping("/admin/update-admin")
    fun updateAdmin(
        @RequestParam id: Long,
        @RequestParam firstname: String,
        @RequestParam lastname: String,
        @RequestParam email: String,
        @RequestParam password: String?,
        @RequestParam adminImage: MultipartFile? // ✅ New image
    ): ResponseEntity<Any> {
        val admin = appUserRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        // Handle new image
        if (adminImage != null && !adminImage.isEmpty) {
            // Delete old image
            admin.adminImage?.let { webAppService.deleteImage(it) }
            // Save new image
            admin.adminImage = webAppService.saveFileToUploadFolder(adminImage)
        }

        // Update fields
        admin.firstName = firstname.trim()
        admin.lastName = lastname.trim()
        admin.appUserEmail = email.trim()
        if (!password.isNullOrEmpty()) {
            admin.password = passwordEncoder.encode(password)
        }

        appUserRepository.save(admin)
        return ResponseEntity.ok(mapOf("message" to "Admin updated"))
    }

    @DeleteMapping("/delete-admin/{id}")
    fun deleteAdmin(@PathVariable id: Long): ResponseEntity<out Any> {
        val admin = appUserRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        appUserRepository.delete(admin)
        return ResponseEntity.ok(mapOf("message" to "Admin deleted"))
    }

    @PostMapping("/admin/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<Any> {
        val result = authService.login(email, password)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Email or Password")
        }
    }

    @PostMapping("/signup")
    fun signup(@RequestBody appUser: AppUser): ResponseEntity<out Any> {
        return try {
            val registeredUser = appUserService.registerUser(appUser)
            //Turn AppUser to AppUserDTO
            val appUserDTO = toAppUserDTO(registeredUser)

            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf("Newly Registered User" to appUserDTO)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user")
        }
    }


    fun toAppUserDTO (appUser: AppUser): AppUserDTO {
        return AppUserDTO(
            id = appUser.appUserId,
            firstName = appUser.firstName,
            lastName = appUser.lastName,
            email = appUser.appUserEmail,
            role = appUser.role,
            adminImage = appUser.adminImage
        )
    }
}