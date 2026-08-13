package com.example.onboardflow.api.exception

import com.example.onboardflow.domain.exceptions.ErrorOccurrenceException
import com.example.onboardflow.domain.exceptions.UserAlreadyExistsException
import com.example.onboardflow.domain.exceptions.UserIsNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    @ExceptionHandler(UserIsNotFoundException::class)
    fun handleUserNotFound(ex: UserIsNotFoundException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.message)
    }

    @ExceptionHandler(ErrorOccurrenceException::class)
    fun handleErrorOccurrence(ex: ErrorOccurrenceException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.message)
    }
}