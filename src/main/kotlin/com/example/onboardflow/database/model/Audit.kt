package com.example.onboardflow.database.model

import jakarta.persistence.*
import java.time.Instant
import java.util.*


@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val actorId:  Long? = null,
    val action: String,
    val entityType: String?=null,
    val entityId:  Long? = null,
    val ipAddress: String? = null,
    val details: String? = null,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null
)
