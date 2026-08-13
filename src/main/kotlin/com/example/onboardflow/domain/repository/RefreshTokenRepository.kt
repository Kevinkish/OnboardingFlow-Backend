package com.example.onboardflow.domain.repository

import com.example.onboardflow.domain.model.RefreshToken
import com.example.onboardflow.domain.model.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    @Query("SELECT n FROM RefreshToken n WHERE n.user = :user AND n.hashedToken = :hashedToken")
    fun findByUserIdAndHashedToken(
        @Param("user") user: User,
        @Param("hashedToken") hashedToken: String
    ): RefreshToken?

    @Query("SELECT n FROM RefreshToken n WHERE n.user = :user AND n.hashedToken = :hashedToken")
    fun deleteByUserIdAndHashedToken(
        @Param("user") user: User,
        @Param("hashedToken") hashedToken: String
    )

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken n WHERE n.user = :user")
    fun deletedStoredRefreshTokenByUser(@Param("user") user: User)
}