package com.example.onboardflow.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

//    private val accesTokenValidityMs = 10L * 24 * 60L * 60L * 1000L  //Validité 10 jours
    private val accesTokenValidityMs = 60L * 60L * 1000L //Validité 1h

    val refreshTokenValidityMs = 30L * 24 * 60L * 60L * 1000L //Validité 30 jours

    private fun generateToken(
        userId: UUID,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder().subject(userId.toString()).claim("type", type).issuedAt(now).expiration(expiryDate)
            .signWith(
                secretKey,
                Jwts.SIG.HS256
            ).compact()
    }

    fun generatedAccessToken(userId: UUID): String {
        return generateToken(userId, "access", accesTokenValidityMs)
    }

    fun generatedRefreshToken(userId: UUID): String {
        return generateToken(userId, "refresh", refreshTokenValidityMs)
    }

    fun validatedAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validatedRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): String {

        val claims = parseAllClaims(token) ?: throw IllegalArgumentException("Invalid token.")
        return claims.subject
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else
            token
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(rawToken).payload
        } catch (e: Exception) {
            null
        }
    }
}