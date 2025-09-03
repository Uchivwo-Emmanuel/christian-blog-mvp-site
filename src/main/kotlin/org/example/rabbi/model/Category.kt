package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var title: String? = null,
    var imageName: String? = null,
    @Lob
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @OneToMany(mappedBy = "category" , fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var webPosts: MutableList<WebPost> = mutableListOf(),
    @ManyToOne
    var appUser: AppUser? = null,
    @CreationTimestamp @Temporal(TemporalType.TIMESTAMP)
    var createdOn: LocalDateTime? = null,
    @UpdateTimestamp @Temporal(TemporalType.TIMESTAMP)
    var updatedOn: LocalDateTime? = null
)
