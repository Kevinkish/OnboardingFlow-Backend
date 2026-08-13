package com.example.onboardflow.application.service

import com.example.onboardflow.api.controllers.AuthControllers
import com.example.onboardflow.domain.model.RefreshToken
import com.example.onboardflow.domain.model.User
import com.example.onboardflow.domain.model.UserStatusEnum
import com.example.onboardflow.domain.repository.RefreshTokenRepository
import com.example.onboardflow.domain.repository.UserRepository
import com.example.onboardflow.infrastructure.security.HashEncoder
import com.example.onboardflow.infrastructure.security.JwtService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
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

    //USER
    fun register(
        email: String,
        password: String,
        phone: String,
        fullName: String,
        hashedPinCode: String,
        status: UserStatusEnum,
        lastLoginAt: Instant?,
        phoneVerified: Boolean,
        createdAt: Instant,
        updatedAt: Instant
    ): User {
        return userRepository.save(
            User(
                email = email.trim(),
                hashedPassword = hashEncoder.encode(password),
                fullName = fullName.trim().replace(" ", "_"),
                phone = phone.trim(),
                hashedPinCode = hashEncoder.encode(hashedPinCode),
                status = status,
                lastLoginAt = lastLoginAt,
                phoneVerified = phoneVerified,
                createdAt = createdAt,
                updatedAt = updatedAt,
                userRole = null,
            )
        )
    }

    @Transactional
    fun updateUser(newUser: AuthControllers.UserUpdateRequest, newUserId: UUID): User {
        val user = userRepository.findUserById(newUserId)
            ?: throw ResponseStatusException(
                HttpStatusCode.valueOf(401),
                "User is null"
            )
        if (with(newUser) {
                (fullName.isNullOrBlank() && email.isNullOrBlank() && hashedPassword.isNullOrBlank() && phone.isNullOrBlank() && hashedPinCode.isNullOrBlank()
                        && phoneVerified == null && lastLoginAt == null && status == null) || (hashedPinCode != null && (!hashedPinCode.all { it.isDigit() } || hashedPinCode.length != 4))


            }) {
            throw BadCredentialsException("All fields are null or a field is not allowed")
        }
        with(newUser) {
            fullName?.let { user.fullName = it.trim().replace(" ", "_") }
            email?.let { user.email = it.trim() }
            hashedPassword?.let { user.hashedPassword = hashEncoder.encode(it) }
            phone?.let { user.phone = it.trim() }
            hashedPinCode?.let { user.hashedPinCode = hashEncoder.encode(it) }
            status?.let { user.status = it }
            lastLoginAt?.let { user.lastLoginAt = it }
            phoneVerified?.let { user.phoneVerified = it }
            user.updatedAt = Instant.now()
        }

        return userRepository.save(user);
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email) ?: throw BadCredentialsException("Invalid credentials")
        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials")
        }
        val newAccesToken = jwtService.generatedAccessToken(user.id!!)
        val newRefreshToken = jwtService.generatedRefreshToken(user.id!!)

        storeRefreshToken(user, newRefreshToken)

        return TokenPair(
            accessToken = newAccesToken,
            refreshToken = newRefreshToken
        )
    }


    //TOKENS
    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validatedRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token.")
        }

        val userId = UUID.fromString(jwtService.getUserIdFromToken(refreshToken))
        val user =
            userRepository.findUserById(userId) ?: throw IllegalArgumentException("Invalid refresh token.")
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user, hashed)
            ?: throw IllegalArgumentException("Refresh token not recognized (maybe used or expired)")
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