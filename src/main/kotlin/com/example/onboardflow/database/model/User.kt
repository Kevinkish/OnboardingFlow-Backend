package com.example.onboardflow.database.model

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    var hashedPassword: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @Column(nullable = false, unique = true)
    var fullName: String,

    var hashedPinCode: String,

    @Enumerated(EnumType.STRING)
    var status: UserStatusEnum,

    var lastLoginAt: Instant? = null,

    var phoneVerified: Boolean = false,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userRole: UserRole? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val refreshTokens: MutableSet<RefreshToken> = mutableSetOf(),
)

