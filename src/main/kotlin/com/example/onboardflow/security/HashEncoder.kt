package com.example.onboardflow.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(raw: String): String = bcrypt.encode(raw).toString()

    fun matches(raw: String, hashed: String): Boolean = bcrypt.matches(raw, hashed)
}