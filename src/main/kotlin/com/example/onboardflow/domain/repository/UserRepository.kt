package com.example.onboardflow.domain.repository

import com.example.onboardflow.domain.model.RoleEnum
import com.example.onboardflow.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {

    @Query("SELECT n FROM User n WHERE n.email = :email")
    fun findByEmail(@Param("email") email: String): User?

    @Query("SELECT n FROM User n WHERE n.fullName = :fullName")
    fun findByFullName(@Param("fullName") fullName: String): User?

    @Query("SELECT n FROM User n WHERE n.id = :id")
    fun findUserById(@Param("id") id: UUID): User?

    @Query(
        """
        SELECT u FROM User u 
        WHERE (:role IS NULL OR u.role = :role)
          AND (:isEmailVerified IS NULL OR u.isEmailVerified = :isEmailVerified)
          AND (
               :search IS NULL 
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
          )
        """
    )
    fun findAllWithFilters(
        @Param("role") role: RoleEnum?,
        @Param("isEmailVerified") isEmailVerified: Boolean?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<User>
}