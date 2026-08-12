package com.example.onboardflow.controllers

import com.example.onboardflow.database.model.RoleEnum
import com.example.onboardflow.database.model.User
import com.example.onboardflow.database.model.UserStatusEnum
import com.example.onboardflow.database.repository.UserRepository
import com.example.onboardflow.database.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthControllers(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {
    data class UserRegistrationRequest(
        val email: String,
        val password: String,
        val phone: String,
        val fullName: String
    )

    data class LoginRequest(
        val email: String,
        val password: String,
    )

    data class RefreshTokenRequest(
        val refreshToken: String
    )

    data class MeProfileResponse(
        val email: String,
        val phone: String,
        val fullName: String,
        val status: UserStatusEnum,
    )

    data class UserUpdateRequest(
        val email: String? = null,
        val hashedPassword: String? = null,
        val phone: String? = null,
        val fullName: String? = null,
        val hashedPinCode: String? = null,
        val status: UserStatusEnum? = null,
        val lastLoginAt: Instant? = null,
        val phoneVerified: Boolean? = null,
    )

    fun getConnectedUser()
            : User {
        val connectedUserId = UUID.fromString(SecurityContextHolder.getContext().authentication?.principal.toString())
        val user = userRepository.findUserById(connectedUserId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        if (user.status != UserStatusEnum.ACTIVE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return user
    }

    fun checkAdmin() {
        if (getConnectedUser().userRole?.role?.designation != RoleEnum.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }

    //USER REGISTRATION & CRUD
    @PostMapping("/register")
    fun register(
        @RequestBody body: UserRegistrationRequest
    ): User {
        if (userRepository.findByEmail(body.email.trim()) != null || userRepository.findByPhone(body.phone.trim()) != null || userRepository.findByFullName(
                body.fullName.trim().replace(" ", "_")
            ) != null
        ) {
            throw ResponseStatusException(
                HttpStatusCode.valueOf(401),
                "User with this email or phone number already exist"
            )
        } else {
            return authService.register(
                email = body.email, password = body.password,
                phone = body.phone,
                fullName = body.fullName,
                hashedPinCode = "0000",
                lastLoginAt = null,
                phoneVerified = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                status = UserStatusEnum.ACTIVE
            )
        }

    }

    @GetMapping("user/all")
    fun getAllUsers(): List<MeProfileResponse> {
        if (getConnectedUser().id != null) {
            checkAdmin()
            return userRepository.findAll().map { user -> user.toResponse() }
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping(path = ["user/{id}"])
    fun findUserById(
        @PathVariable(required = true) id: UUID
    ): MeProfileResponse {
        if (getConnectedUser().id != null) {
            checkAdmin()
            val user = userRepository.findUserById(id)
            if (user == null) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id not found")
            } else {
                return user.toResponse();
            }
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id not found")
        }
    }

    @PutMapping(path = ["user/{id}"])
    fun updateUserById(
        @PathVariable(required = true) id: UUID,
        @RequestBody body: UserUpdateRequest
    ): UserUpdateRequest {
        if (getConnectedUser().id != null) {
            checkAdmin()

            if (body.email != null && userRepository.findByEmail(body.email.trim()) != null || body.phone != null && userRepository.findByPhone(
                    body.phone.trim()
                ) != null
            ) {
//            throw IllegalArgumentException("User with this email already exist")
                throw ResponseStatusException(
                    HttpStatusCode.valueOf(401),
                    "User with this email or phone number already exist"
                )
            } else {
                return authService.updateUser(newUserId = id, newUser = body).toUpdate()
            }
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id not found")
        }
    }

    @DeleteMapping(path = ["user/{id}"])
    fun deleteUserById(
        @PathVariable id: UUID
    ) {
        if (getConnectedUser().id != null) {
            checkAdmin()

            val user = userRepository.findUserById(id) ?: throw IllegalArgumentException("Note not found")
            userRepository.deleteById(
                user.id ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with this id not found"
                )
            )
        }

    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthService.TokenPair {
        return authService.login(body.email, body.password)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshTokenRequest
    ): AuthService.TokenPair {
        return authService.refresh(body.refreshToken)
    }

    @GetMapping("/me")
    fun profile(
    ): MeProfileResponse {
        val user = userRepository.findUserById(
            getConnectedUser().id ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User with this id not found"
            )
        )
        if (user == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id not found")
        } else {
            return user.toResponse();
        }

    }

    private fun User.toResponse(): MeProfileResponse {
        return MeProfileResponse(
            email = email,
            phone = phone,
            fullName = fullName,
            status = status,

            );
    }

    private fun User.toUpdate(): UserUpdateRequest {
        return UserUpdateRequest(
            email = email,
            phone = phone,
            fullName = fullName,
            status = status,
            hashedPassword = hashedPassword,
            hashedPinCode = hashedPinCode,
            lastLoginAt = lastLoginAt,
            phoneVerified = phoneVerified,
        );
    }


}