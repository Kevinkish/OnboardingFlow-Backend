package com.example.onboardflow.domain.repository

import com.example.onboardflow.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    @Query("SELECT n FROM User n WHERE n.email = :email")
    fun findByEmail(@Param("email") email: String): User?

    @Query("SELECT n FROM User n WHERE n.phone = :phone")
    fun findByPhone(@Param("phone") phone: String): User?

    @Query("SELECT n FROM User n WHERE n.fullName = :fullName")
    fun findByFullName(@Param("fullName") fullName: String): User?

    @Query("SELECT n FROM User n WHERE n.id = :id")
    fun findUserById(@Param("id") id: UUID): User?

//    @Query("SELECT u FROM User u")
//    fun findAllUsers(): List<User?>
}