package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class PostPoint(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    var pointHeading: String? = null,
    @Column(name = "point_body", columnDefinition = "TEXT")
    var pointBody: String? = null,
    @ManyToOne @JsonIgnore
    var post: WebPost? = null,
    var pointImageName: String? = null,
    @CreationTimestamp
    var createdOn: LocalDateTime? = null,
    @UpdateTimestamp
    var updatedOn: LocalDateTime? = null
)