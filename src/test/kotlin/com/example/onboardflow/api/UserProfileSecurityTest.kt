package com.example.onboardflow


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileSecurityTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "user-a-id-123", roles = ["USER"])
    fun `should prevent user from accessing another user profile`() {
        val anotherUserId = UUID.randomUUID()

        mockMvc.perform(get("/users/$anotherUserId"))
            .andExpect(status().isForbidden)
    }
}