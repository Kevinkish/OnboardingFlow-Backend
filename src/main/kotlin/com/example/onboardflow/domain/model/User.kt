package com.example.onboardflow.domain.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    @field:NotBlank(message = "Mail is mandatory")
    var email: String,

    @field:NotBlank(message = "Password is mandatory")
    var hashedPassword: String,

    @field:NotBlank(message = "Name is mandatory")
    var fullName: String,

    @Enumerated(EnumType.STRING)
    var status: UserStatusEnum,

    var lastLoginAt: Instant? = null,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userRole: UserRole? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val refreshTokens: MutableSet<RefreshToken> = mutableSetOf(),
)

