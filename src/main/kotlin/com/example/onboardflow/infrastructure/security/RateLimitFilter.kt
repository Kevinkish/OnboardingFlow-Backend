package com.example.onboardflow.infrastructure.security


import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitingFilter : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.endsWith("/auth/login") && request.method.equals("POST", ignoreCase = true)) {
            val clientIp = extractClientIp(request)
            val bucket = buckets.computeIfAbsent(clientIp) { createNewBucket() }

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response)
            } else {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json;charset=UTF-8"
                response.writer.write(
                    """
                    {
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Too many login request. Please try again in 1 minute"
                    }
                    """.trimIndent()
                )
            }
        } else {
            filterChain.doFilter(request, response)
        }
    }

    private fun createNewBucket(): Bucket {
        val limit = Bandwidth.builder()
            .capacity(5)
            .refillIntervally(5, Duration.ofMinutes(1))
            .build()
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrEmpty()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }
}