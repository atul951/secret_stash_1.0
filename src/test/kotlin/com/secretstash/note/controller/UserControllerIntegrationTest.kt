package com.secretstash.note.controller

import com.secretstash.note.BaseIntegrationTest
import com.secretstash.note.dto.user.UserResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get

class UserControllerIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `when given valid request, should update user note successfully`() {
        // GIVEN - user already exists
        // WHEN - call fetch user details api
        val response = mockMvc.get("/api/user") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the token
        val userResponse = objectMapper.readValue(response, UserResponseDto::class.java)

        // THEN - assert username
        assertEquals(username, userResponse.username)
        assertEquals("base_test@example.com", userResponse.email)
    }
}
