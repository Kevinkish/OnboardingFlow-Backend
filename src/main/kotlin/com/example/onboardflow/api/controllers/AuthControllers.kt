package com.example.onboardflow.api.controllers

import com.example.onboardflow.application.service.AuthService
import com.example.onboardflow.domain.model.User
import com.example.onboardflow.domain.model.UserStatusEnum
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant


@RestController
@RequestMapping("/auth")
class AuthControllers(
    private val authService: AuthService,
) {
    data class UserRegistrationRequest(
        @field:NotBlank(message = "Email is required")
        @field:Email(message = "Please provide a valid email address")
        val email: String?,

        @field:NotBlank(message = "Password is required")
        @field:Size(min = 8, message = "Password must be at least 8 characters long")
        @field:Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character (ex: Password123@)"
        )
        val password: String?,

        @field:NotBlank(message = "Full name is required")
        @field:Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        val fullName: String?
    )

    data class LoginRequest(
        @field:NotBlank(message = "Mail is mandatory")
        val email: String?,
        @field:NotBlank(message = "Password is mandatory")
        val password: String?,
    )

    data class RefreshTokenRequest(
        val refreshToken: String
    )

    data class MeProfileResponse(
        val email: String,
        val fullName: String,
        val status: UserStatusEnum,
        val lastLoginAt: Instant? = null

    )

    data class UserUpdateRequest(
        val password: String? = null,
        val fullName: String? = null,
        val status: UserStatusEnum? = null,
        val lastLoginAt: Instant? = null,
    )

    @PostMapping("/register")
//    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody body: UserRegistrationRequest
    ): MeProfileResponse {
        val newUser = authService.register(
            email = body.email,
            password = body.password,
            fullName = body.fullName,
            lastLoginAt = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            status = UserStatusEnum.PENDING_VERIFICATION
        )
        return newUser.toResponse()
    }

    @GetMapping("/verify-email")
    fun verifyEmail(@RequestParam("token") token: String): ResponseEntity<String> {
        authService.verifyEmail(token)
        return ResponseEntity.ok("Successfully verified email ! You know have full access")
    }

    @PutMapping(path = ["/me"])
    fun updateUserMe(
        @RequestBody body: UserUpdateRequest
    ): MeProfileResponse {
        return authService.updateUserMe(body = body).toResponse()
    }

//    @DeleteMapping(path = ["user/{id}"])
//    fun deleteUserById(
//        @PathVariable id: UUID
//    ) {
//        if (getConnectedUser().id != null) {
//            checkAdmin()
//
//            val user = userRepository.findUserById(id) ?: throw IllegalArgumentException("Note not found")
//            userRepository.deleteById(
//                user.id ?: throw ResponseStatusException(
//                    HttpStatus.NOT_FOUND,
//                    "User with this id not found"
//                )
//            )
//        }
//    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthService.TokenPair {
        return authService.login(body.email, body.password)
    }

//    @GetMapping("/get-token")
//    fun getToken(
//     ): RefreshToken {
//        return authService.getRefreshToken()
//    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshTokenRequest
    ): AuthService.TokenPair {
        return authService.refresh(body.refreshToken)
    }

    @GetMapping("/me")
    fun profile(
    ): MeProfileResponse {
        return authService.profile().toResponse()
    }

    private fun User.toResponse(): MeProfileResponse {
        return MeProfileResponse(
            email = email,
            fullName = fullName,
            status = status,
            lastLoginAt = lastLoginAt
        );
    }


}