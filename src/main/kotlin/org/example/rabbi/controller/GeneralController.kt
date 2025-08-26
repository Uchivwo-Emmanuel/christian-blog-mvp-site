package org.example.rabbi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.servlet.http.HttpServletRequest
import org.example.rabbi.model.AppUser
import org.example.rabbi.model.Category
import org.example.rabbi.repository.AppUserRepository
import org.springframework.ui.Model
import org.example.rabbi.repository.CategoryRepository
import org.example.rabbi.repository.WebPostRepository
import org.example.rabbi.service.AppUserService
import org.example.rabbi.service.WebAppService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Controller
@RequestMapping("/")
class GeneralController(
    private val categoryRepository: CategoryRepository,
    private val webAppService: WebAppService,
    private val webPostRepository: WebPostRepository,
    private val appUserRepository: AppUserRepository,
    private val appUserService: AppUserService,
    private val controllerForRestApi: ControllerForRestApi,
    private val passwordEncoder: PasswordEncoder
) {
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

    /*@GetMapping("/create-category")
    fun showCategoryForm(): String {
        return "create-category" // This loads create-category.html
    }*/
    @GetMapping("/admin/make-post")
    fun showPostForm(): String {
        return "post-creation" // This loads create-category.html
    }

    @GetMapping("/view-post/{id}")
    fun showPostPage(@PathVariable id: Long, model: Model): String {
        val post = webPostRepository.findWithPointById(id)
            ?: throw RuntimeException("Post with ID $id not found")

        val pageTitle = "${post.title} | Rabbi's Scrolls"

        model.addAttribute("post", post)
        model.addAttribute("category", post.category) // Optional: for breadcrumb
        model.addAttribute("pageTitle", pageTitle) // 👈 Add this
        return "post"
    }

    @GetMapping("/")
    fun showCategories(model: Model): String {
        model.addAttribute("categories", webAppService.categoriesToDTO(3))
        model.addAttribute("posts", webAppService.getAllPostsSorted(10))
        return "index" // This should match your HTML file name in templates (e.g., categories.html)
    }

    /*@GetMapping("/admin")
    fun showLoginForm(model: Model): String {
        return "/admin/login"  // Returns login.html
    }

    @GetMapping("/admin/manage-admin")
    fun  showManageAdmin(model: Model): String{
        return "manage-admin"
    }


    @GetMapping("/admin/manage-categories")
    fun showManageCategories(model: Model): String {
        model.addAttribute("categories", categoryRepository.findAll())
        return "manage-categories"  // Returns login.html
    }

    @GetMapping("/admin/manage-posts")
    fun showManagePosts(model: Model): String {
        // In your controller
        val objectMapper = ObjectMapper().registerKotlinModule()

        val posts = webPostRepository.findAll().map { post ->
            mapOf(
                "id" to post.id,
                "title" to post.title,
                "introduction" to post.introduction,
                "categoryName" to post.category?.title,
                "titleImageName" to post.titleImageName,
                "pointsJson" to try {
                    if (post.points.isEmpty()) "[]" else objectMapper.writeValueAsString(
                        post.points.map { p ->
                            mapOf(
                                "pointTitle" to p.pointHeading,
                                "pointBody" to p.pointBody,
                                "pointImageName" to p.pointImageName
                            )
                        }
                    )
                } catch (e: Exception) {
                    "[]"
                }
            )
        }

        model.addAttribute("posts", posts)
        model.addAttribute("categories", categoryRepository.findAll())
        return "manage-post"  // Returns manage-post.html
    }
*/

    /*@PostMapping("/logout")
    fun logout(request: HttpServletRequest,
               response: HttpServletResponse,
               model: Model
    ): String{
        //Get current user session without creating a new one
        val currentSession = request.getSession(false)
        //invalidate session
        currentSession?.invalidate()
        //clear JWT token by removing it from header or setting it to expired state
        response.setHeader("Authorization","")
        ResponseEntity.status(HttpStatus.OK).body("Successfully Logged out")
        return "login"
    }*/

    @GetMapping("/about")
    fun showAboutPage(model: Model): String {
        return "about"  // Returns login.html
    }

    /*@GetMapping("/admin/create-category")
    fun showCreateCategoryForm(model: Model): String {
        return "/admin/create-category"  // Returns login.html
    }*/


    /*@GetMapping("/admin/entry")
    fun adminEntry(
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        *//*if (userDetails == null) {
            return "redirect:/admin"
        }*//*

        val email = userDetails?.username
        val user = email?.let { appUserRepository.findByAppUserEmail(it) }
            ?: return "redirect:/admin"

        if (user.role != "ADMIN") {
            return "redirect:/admin"
        }

        model.addAttribute("currentUser", controllerForRestApi.toAppUserDTO(user))
        return "/admin/admin-dashboard"
    }*/

    /*@GetMapping("/admin/dashboard")
    fun showDashboard(
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {

        if (userDetails == null) {
            println("🔴 No authentication found")
            return "redirect:/admin"
        }

        // 2. Get email from UserDetails
        val email = userDetails.username

        // 3. Load full AppUser from database
        val appUser = appUserRepository.findByAppUserEmail(email)
            ?: return "redirect:/admin"

        // 4. Ensure user is ADMIN
        if (appUser.role != "ADMIN") {
            return "redirect:/admin"
        }

        // 5. Add user to model for Thymeleaf
        val userDTO = controllerForRestApi.toAppUserDTO(appUser)
        model.addAttribute("currentUser", userDTO)

        // 6. Render dashboard
        return "admin/admin-dashboard"
    }*/

    @PostMapping("/admin/signup")
    fun signupAdmin(
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam appUserEmail: String,
        @RequestParam phoneNumber: String?,
        @RequestParam password: String,
        @RequestParam adminImage: MultipartFile?, // ✅ Optional image
        authentication: Authentication?,
        model: Model
    ): ResponseEntity<*> {
        /*// 1. Check if user is authenticated
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication required.")

        // 2. Load current user
        val currentUser = appUserRepository.findByAppUserEmail(email)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Current user not found.")

        // 3. Only admins can create new admins
        if (currentUser.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Admins only.")
        }*/

        // 4. Validate input
        if (firstName.isBlank() || lastName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("First and last name are required.")
        }

        if (!isValidEmail(appUserEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid email format.")
        }

        if (password.length < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Password must be at least 6 characters.")
        }

        // 5. Check if email already exists
        if (appUserRepository.findByAppUserEmail(appUserEmail.lowercase()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("User with this email already exists.")
        }

        // 6. Save image if provided
        val imageName = adminImage?.let { file ->
            if (file.isEmpty) null else webAppService.saveFileToUploadFolder(file)
        }

        // 7. Create new admin
        val newAdmin = AppUser(
            appUserId = null,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            appUserEmail = appUserEmail.lowercase().trim(),
            phoneNumber = phoneNumber?.trim(),
            adminImage = imageName,
            role = "ADMIN",
            password = passwordEncoder.encode(password),
            createdOn = LocalDateTime.now(),
            updatedOn = LocalDateTime.now()
        )

        // 8. Save and return DTO
        return try {
            val savedUser = appUserService.registerUser(newAdmin)
            val userDTO = controllerForRestApi.toAppUserDTO(savedUser)

            ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                "message" to "Admin registered successfully",
                "user" to userDTO
            ))
        } catch (e: Exception) {
            // Clean up uploaded image if save fails
            imageName?.let { webAppService.deleteImage(it) }

            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error registering admin: ${e.message}")
        }
    }

    /*@PostMapping("/admin/first-signup")
    fun registerAdmin(
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam appUserEmail: String,
        @RequestParam phoneNumber: String?,
        @RequestParam password: String,
        @RequestParam adminImage: MultipartFile? // ✅ Optional image
    ): ResponseEntity<Any> {
        // Check if email exists
        if (appUserRepository.findByAppUserEmail(appUserEmail.lowercase()) != null) {
            return ResponseEntity.status(400).body("Email already exists")
        }

        // Save image if provided
        val imageName = adminImage?.let { webAppService.saveFileToUploadFolder(it) }

        // Create new admin
        val admin = AppUser(
            appUserId = null,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            appUserEmail = appUserEmail.lowercase().trim(),
            phoneNumber = phoneNumber?.trim(),
            adminImage = imageName,
            role = "ADMIN",
            password = passwordEncoder.encode(password),
            createdOn = null,
            updatedOn = null
        )

        appUserRepository.save(admin)
        return ResponseEntity.ok(mapOf("message" to "Admin registered successfully"))
    }*/

    // Helper
    private fun badRequest(msg: String) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg)

    private fun isValidEmail(email: String): Boolean =
        """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex().matches(email)

    @GetMapping("/category/{id}")
    fun getCategoryPage(@PathVariable id: Long, model: Model): String {
        val category = categoryRepository.findByIdWithPosts(id)
            ?: throw RuntimeException("Category with $id was not found in repository")

        // ✅ Build the full page title in Kotlin
        val pageTitle = "${category.title} | Rabbi's Scrolls"

        model.addAttribute("category", category)
        model.addAttribute("posts", category.webPosts)
        model.addAttribute("pageTitle", pageTitle) // 👈 Add this
        model.addAttribute("categories", categoryRepository.findAll()) // for nav
        return "category-page"
    }

}