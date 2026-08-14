package com.example.onboardflow.infrastructure.config


import com.example.onboardflow.domain.model.RoleEnum
import com.example.onboardflow.domain.model.User
import com.example.onboardflow.domain.model.UserStatusEnum
import com.example.onboardflow.domain.repository.UserRepository
import com.example.onboardflow.infrastructure.security.HashEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
class DatabaseSeeder(
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        val adminEmail = "admin@onboardflow.com"

        if (userRepository.findByEmail(adminEmail) == null) {
            val adminUser = User(
                email = adminEmail,
                hashedPassword = hashEncoder.encode("AdminPass123!"),
                fullName = "System Administrator",
                role = RoleEnum.ADMIN,
                isEmailVerified = true,
                status = UserStatusEnum.ACTIVE,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            userRepository.save(adminUser)
            log.info("🔑 Seeded default administrator account: {}", adminEmail)
        } else {
            log.info("ℹ️ Default administrator account already exists.")
        }
    }
}