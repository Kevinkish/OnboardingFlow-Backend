package com.example.onboardflow.application.service


import com.example.onboardflow.api.controllers.AuthControllers
import com.example.onboardflow.api.controllers.toResponse
import com.example.onboardflow.api.dto.PageResponse
import com.example.onboardflow.domain.exceptions.CustomForbiddenException
import com.example.onboardflow.domain.exceptions.CustomNotFoundException
import com.example.onboardflow.domain.exceptions.ErrorOccurrenceException
import com.example.onboardflow.domain.model.RoleEnum
import com.example.onboardflow.domain.model.User
import com.example.onboardflow.domain.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminUserService(
    private val userRepository: UserRepository
) {

    fun getConnectedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            throw ErrorOccurrenceException("Unauthenticated user or expired token")
        }

        val userIdString = authentication.principal.toString()
        val connectedUserId = try {
            UUID.fromString(userIdString)
        } catch (e: IllegalArgumentException) {
            throw ErrorOccurrenceException("Invalid user")
        }

        return userRepository.findUserById(connectedUserId)
            ?: throw CustomNotFoundException("User not found")
    }


    @Transactional(readOnly = true)
    fun getUsers(
        role: RoleEnum?,
        isEmailVerified: Boolean?,
        searchQuery: String?,
        pageable: Pageable
    ): PageResponse<AuthControllers.MeProfileResponse> {

        if (getConnectedUser().role != RoleEnum.ADMIN) {
            throw CustomForbiddenException("Forbidden path")
        }
        val cleanSearch = searchQuery?.takeIf { it.isNotBlank() }

        val usersPage = userRepository.findAllWithFilters(
            role = role,
            isEmailVerified = isEmailVerified,
            search = cleanSearch,
            pageable = pageable
        )

        return PageResponse(
            content = usersPage.content.map { it.toResponse() },
            pageNumber = usersPage.number,
            pageSize = usersPage.size,
            totalElements = usersPage.totalElements,
            totalPages = usersPage.totalPages,
            isLast = usersPage.isLast
        )
    }
}