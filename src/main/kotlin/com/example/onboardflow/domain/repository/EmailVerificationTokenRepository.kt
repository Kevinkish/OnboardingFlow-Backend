package com.example.onboardflow.domain.repository

import com.example.onboardflow.domain.model.EmailVerificationToken
import com.example.onboardflow.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {

    @Query("SELECT n FROM EmailVerificationToken n WHERE n.hashedToken = :hashedToken")
    fun findByHashedToken(
        @Param("hashedToken") hashedToken: String
    ): EmailVerificationToken?

    @Transactional
    @Modifying
    @Query("DELETE FROM EmailVerificationToken n WHERE n.user = :user")
    fun deleteByUser(
        @Param("user") user: User
    )
}