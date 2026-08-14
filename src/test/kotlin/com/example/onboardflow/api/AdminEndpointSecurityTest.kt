package com.example.onboardflow.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AdminEndpointSecurityTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "user@test.com", roles = ["USER"])
    fun `should deny access to admin endpoint for regular users`() {
        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isForbidden) // Expected : HTTP 403
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `should allow access to admin endpoint for admin users`() {
        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk) // Expected : HTTP 200
    }
}