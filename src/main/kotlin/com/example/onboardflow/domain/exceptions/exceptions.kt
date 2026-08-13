package com.example.onboardflow.domain.exceptions

class UserAlreadyExistsException(message: String) : RuntimeException(message)

class ErrorOccurrenceException(message: String) : RuntimeException(message)

class UserIsNotFoundException(message: String) : RuntimeException(message)