package com.example.onboardflow.application.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.email.from}") private val fromEmail: String,
    @Value("\${app.email.verification-base-url}") private val verificationBaseUrl: String
) {

    @Async
    fun sendVerificationEmail(toEmail: String, hashedToken: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        val verificationUrl = "$verificationBaseUrl?token=$hashedToken"

        helper.setFrom(fromEmail)
        helper.setTo(toEmail)
        helper.setSubject("Onboardflow account verification")

        // Mail HTML content
        val htmlContent = """
            <h2>Welcome to our Onboarding application !</h2>
            <p>Please click on the link below to verify your mail :</p>
            <a href="$verificationUrl" style="padding: 10px 15px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Verify your mail</a>
            <p>Or copy the lien in your browser : $verificationUrl</p>
        """.trimIndent()

        helper.setText(htmlContent, true)

        mailSender.send(message)
    }
}