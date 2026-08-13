package com.example.onboardflow.api.exception

import com.example.onboardflow.domain.exceptions.CustomForbiddenException
import com.example.onboardflow.domain.exceptions.CustomNotFoundException
import com.example.onboardflow.domain.exceptions.ErrorOccurrenceException
import com.example.onboardflow.domain.exceptions.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.message)
    }

    @ExceptionHandler(CustomNotFoundException::class)
    fun handleNotFound(ex: CustomNotFoundException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.message)
    }
    @ExceptionHandler(CustomForbiddenException::class)
    fun handleForbidden(ex: CustomForbiddenException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ex.message)
    }

    @ExceptionHandler(ErrorOccurrenceException::class)
    fun handleErrorOccurrence(ex: ErrorOccurrenceException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        val body = mapOf(
            "status" to 400,
            "error" to "Bad Request",
            "message" to "Input validation failed",
            "fieldErrors" to errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}