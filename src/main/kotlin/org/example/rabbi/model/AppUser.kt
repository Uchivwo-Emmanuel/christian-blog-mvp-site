package org.example.rabbi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
class AppUser (
    @GeneratedValue @Id
    var appUserId: Long?,
    var firstName: String?,
    var lastName: String?,
    var appUserEmail: String?,
    var phoneNumber: String?,
    var adminImage: String?,
    var role: String?,// ADMIN, USER
    var password: String?,
    @CreationTimestamp @Temporal(TemporalType.TIMESTAMP)
    var createdOn: LocalDateTime?,
    @CreationTimestamp @Temporal(TemporalType.TIMESTAMP)
    var updatedOn: LocalDateTime?,
    @JsonIgnore
    @OneToMany(mappedBy = "appUser" , fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var categories: MutableList<Category> = mutableListOf(),
    @JsonIgnore
    @OneToMany(mappedBy = "appUser" , fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var webPosts: MutableList<WebPost> = mutableListOf()
)