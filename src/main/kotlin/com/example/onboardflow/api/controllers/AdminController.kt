package com.example.onboardflow.api.controllers


import com.example.onboardflow.application.service.AdminUserService
import com.example.onboardflow.domain.model.RoleEnum
import com.example.onboardflow.api.dto.PageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/users")
class AdminUserController(
    private val adminUserService: AdminUserService
) {

    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false) role: RoleEnum?,
        @RequestParam(required = false) isEmailVerified: Boolean?,
        @RequestParam(required = false) search: String?,

        // Spring intercepte : ?page=0&size=10&sort=createdAt,desc
        @PageableDefault(page = 0, size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<PageResponse<AuthControllers.MeProfileResponse>> {

        val response = adminUserService.getUsers(role, isEmailVerified, search, pageable)
        return ResponseEntity.ok(response)
    }
}