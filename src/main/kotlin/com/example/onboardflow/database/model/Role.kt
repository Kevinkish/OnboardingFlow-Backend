package com.example.onboardflow.database.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Enumerated(EnumType.STRING)
    var designation: RoleEnum? = null,
    var description: String? = null,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    val createdById: Long? = null,

    @OneToMany(mappedBy = "role", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userRole: MutableSet<UserRole> = mutableSetOf(),
)

@Entity
@Table(name = "user_roles")
class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val assignedAt: Instant = Instant.now(),
    var active: Boolean,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),

    val createdById: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    var role: Role? = null
)
