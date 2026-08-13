package com.example.onboardflow.domain.repository


import com.example.onboardflow.domain.model.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    @Query("SELECT n FROM EmailVerificationToken n WHERE n.hashedToken = :hashedToken")
    fun findByHashedToken(
        @Param("hashedToken") hashedToken: String
    ): EmailVerificationToken?

}