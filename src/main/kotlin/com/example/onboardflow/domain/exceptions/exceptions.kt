package com.example.onboardflow.domain.exceptions

class UserAlreadyExistsException(message: String) : RuntimeException(message)

class ErrorOccurrenceException(message: String) : RuntimeException(message)

class CustomNotFoundException(message: String) : RuntimeException(message)

class CustomForbiddenException(message: String) : RuntimeException(message)