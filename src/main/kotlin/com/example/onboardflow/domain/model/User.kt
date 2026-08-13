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

    var profileImageUrl: String?=null,

    @Enumerated(EnumType.STRING)
    var status: UserStatusEnum,

    var lastLoginAt: Instant? = null,

    var isEmailVerified: Boolean? = false,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleEnum = RoleEnum.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val refreshTokens: MutableSet<RefreshToken> = mutableSetOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val emailVerificationTokens: MutableSet<EmailVerificationToken> = mutableSetOf()
)


@Entity
@Table(name = "email_verification_tokens")
class EmailVerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var hashedToken: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)