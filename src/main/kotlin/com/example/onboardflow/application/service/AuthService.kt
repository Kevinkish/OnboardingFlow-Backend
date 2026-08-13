package com.example.onboardflow.application.service

import com.example.onboardflow.api.controllers.AuthControllers
import com.example.onboardflow.domain.exceptions.CustomNotFoundException
import com.example.onboardflow.domain.exceptions.ErrorOccurrenceException
import com.example.onboardflow.domain.exceptions.UserAlreadyExistsException
import com.example.onboardflow.domain.model.RefreshToken
import com.example.onboardflow.domain.model.User
import com.example.onboardflow.domain.model.UserStatusEnum
import com.example.onboardflow.domain.repository.RefreshTokenRepository
import com.example.onboardflow.domain.repository.UserRepository
import com.example.onboardflow.infrastructure.security.HashEncoder
import com.example.onboardflow.infrastructure.security.JwtService
import jakarta.transaction.Transactional
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
    )

    fun getConnectedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            throw ErrorOccurrenceException("Unauthenticated user or expired token")
        }

        val userIdString = authentication.principal.toString()
        val connectedUserId = try {
            UUID.fromString(userIdString)
        } catch (e: IllegalArgumentException) {
            throw ErrorOccurrenceException("Invalid user")
        }

        return userRepository.findUserById(connectedUserId)
            ?: throw CustomNotFoundException("User not found")
    }


    fun register(
        email: String?,
        password: String?,
        fullName: String?,
        status: UserStatusEnum,
        lastLoginAt: Instant?,
        createdAt: Instant,
        updatedAt: Instant
    ): User {
        if (listOf(
                email,
                password,
                fullName
            ).any({ it.isNullOrBlank() })
        ) {
            throw BadCredentialsException("Invalid credentials")
        }
        val cleanEmail = email.toString().trim()
        if (userRepository.findByEmail(cleanEmail) != null) {
            throw UserAlreadyExistsException("An error occurred")
        }
        return userRepository.save(
            User(
                email = cleanEmail,
                hashedPassword = hashEncoder.encode(password.toString()),
                fullName = fullName.toString().trim(),
                status = status,
                lastLoginAt = lastLoginAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
                userRole = null,
            )
        )
    }

    fun profile(): User {
        return getConnectedUser()
    }

    @Transactional
    fun updateUserMe(body: AuthControllers.UserUpdateRequest): User {
        val user = getConnectedUser()
        if (with(body) {
                listOf(
                    fullName,
                    password,
                ).all { it.isNullOrBlank() } && listOf(
                    lastLoginAt, status
                ).all {
                    it == null
                }
            }) {
            throw ErrorOccurrenceException("All allowed fields are null")
        }


        with(body) {
            fullName?.let { user.fullName = it.trim() }
            password?.let { user.hashedPassword = hashEncoder.encode(it) }
            status?.let { user.status = it }
            lastLoginAt?.let { user.lastLoginAt = it }
            user.updatedAt = Instant.now()
        }

        return userRepository.save(user);
    }

    fun login(email: String?, password: String?): TokenPair {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            throw ErrorOccurrenceException("Invalid email or password")
        }
        val user = userRepository.findByEmail(email) ?: throw CustomNotFoundException("Invalid credentials")
        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw CustomNotFoundException("Invalid credentials")
        }
        val newAccesToken = jwtService.generatedAccessToken(user.id!!)
        val newRefreshToken = jwtService.generatedRefreshToken(user.id!!)

        storeRefreshToken(user, newRefreshToken)

        return TokenPair(
            accessToken = newAccesToken,
            refreshToken = newRefreshToken
        )
    }

//    fun getRefreshToken(): RefreshToken {
//        return refreshTokenRepository.findLatestRefreshTokenByUser(getConnectedUser()) ?: throw CustomNotFoundException(
//            "User not found"
//        )
//    }

    //TOKENS
    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validatedRefreshToken(refreshToken)) {
            throw ErrorOccurrenceException("Invalid refresh token.")
        }

        val userId = UUID.fromString(jwtService.getUserIdFromToken(refreshToken))
        val user =
            userRepository.findUserById(userId) ?: throw CustomNotFoundException("User not found")
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user, hashed)
            ?: throw ErrorOccurrenceException("Refresh token not recognized (maybe used or expired)")
        refreshTokenRepository.deleteByUserIdAndHashedToken(user, hashed)

        val newAccessToken = jwtService.generatedAccessToken(user.id!!)
        val newRefreshToken = jwtService.generatedRefreshToken(user.id!!)

        storeRefreshToken(user, newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun storeRefreshToken(user: User, rawRefreshToken: String) {
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)
        val tokenUser = userRepository.findUserById(user.id ?: throw BadCredentialsException("User ID is null"))
        //TO DELETE EVERY STORED TOKEN RIGHT BEFORE STORING NEW ONE
        refreshTokenRepository.deletedStoredRefreshTokenByUser(user)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}