package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jdk.jfr.Timestamp
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class WebPost(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var title: String? = null,
    var titleImageName: String? = null,
    @Lob
    @Column(columnDefinition = "TEXT")
    var introduction: String? = null,
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)//Hibernate will cascade the save of points when you save WebPost.
    var points: MutableList<PostPoint> = mutableListOf(),
    @ManyToOne @JsonIgnore
    var category: Category? = null,
    @ManyToOne
    var appUser: AppUser? = null,
    @CreationTimestamp
    var createdOn: LocalDateTime? = null,
    @UpdateTimestamp
    var updatedOn: LocalDateTime? = null
)