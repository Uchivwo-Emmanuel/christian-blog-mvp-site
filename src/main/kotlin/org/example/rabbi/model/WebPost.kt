package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "web_post")
class WebPost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "title", nullable = false)
    var title: String? = null,

    @Column(name = "title_image_name")
    var titleImageName: String? = null,

    @Column(name = "introduction", columnDefinition = "TEXT")
    var introduction: String? = null,

    @OneToMany(
        mappedBy = "webPost",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var points: MutableList<PostPoint> = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    var category: Category? = null,

    @ManyToOne
    @JoinColumn(name = "app_user_app_user_id", nullable = false)
    var appUser: AppUser? = null,

    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    var createdOn: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_on", nullable = false)
    var updatedOn: LocalDateTime? = null
)