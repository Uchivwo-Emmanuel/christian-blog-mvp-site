package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "post_point")
class PostPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "point_heading", nullable = false)
    var pointHeading: String? = null,

    @Column(name = "point_body", columnDefinition = "TEXT")
    var pointBody: String? = null,

    @Column(name = "point_image_name")
    var pointImageName: String? = null,

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    var webPost: WebPost? = null,

    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    val createdOn: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_on", nullable = false)
    var updatedOn: LocalDateTime? = null
)