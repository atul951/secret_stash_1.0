package com.secretstash.note.controller

import com.secretstash.note.BaseIntegrationTest
import com.secretstash.note.dto.ErrorResponseDto
import com.secretstash.note.dto.auth.AuthRequestDto
import com.secretstash.note.dto.auth.AuthResponseDto
import com.secretstash.note.dto.auth.RefreshRequestDto
import com.secretstash.note.dto.auth.UserRegistrationRequestDto
import com.secretstash.note.dto.auth.UserRegistrationResponseDto
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthControllerIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `when given valid input, should register user successfully`() {
        // GIVEN
        val registerRequest = UserRegistrationRequestDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123"
        )

        // WHEN
        val response = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, UserRegistrationResponseDto::class.java)
        assertEquals(registerRequest.username, responseDto.username)
        assertEquals(registerRequest.email, responseDto.email)
    }

    @Test
    fun `when given duplicate username, should fail user registration`() {
        // GIVEN
        val registerRequest = UserRegistrationRequestDto(
            username = this.username,
            email = "user_2@gmail.com",
            password = "password123"
        )
        // WHEN
        val response = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, ErrorResponseDto::class.java)
        assertEquals("User '${this.username}' already exists.", responseDto.message)
    }

    @Test
    fun `when given invalid input, should return bad request for user registration`() {
        // GIVEN
        val invalidRequest = UserRegistrationRequestDto(
            username = "",
            email = "user_1@gmail.com",
            password = "123"
        )

        // WHEN
        val response = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, ErrorResponseDto::class.java)
        assertTrue(responseDto.message.contains("Username can only contain alphanumeric characters"))
    }

    @Test
    fun `when given valid credentials, should login user successfully`() {
        // GIVEN
        val loginRequest = AuthRequestDto(
            username = this.username,
            password = "password123"
        )

        // WHEN
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, AuthResponseDto::class.java)
        assertEquals(loginRequest.username, responseDto.username)
        assertEquals("base_test@example.com", responseDto.email)
    }

    @Test
    fun `when given incorrect username, should fail login`() {
        // GIVEN
        val loginRequest = AuthRequestDto(
            username = "user_2",
            password = "password"
        )

        // WHEN
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isUnauthorized() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, ErrorResponseDto::class.java)
        assertEquals("Invalid username or password", responseDto.message)
    }

    @Test
    fun `when given valid refresh token, should refresh token successfully`() {
        // GIVEN
        val loginRequest = AuthRequestDto(
            username = this.username,
            password = "password123"
        )
        // login
        val loginResponse = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
        // get response
        val authDetails = objectMapper.readValue(loginResponse, AuthResponseDto::class.java)
        val refreshRequestDto = RefreshRequestDto(authDetails.refreshToken)

        // WHEN
        val response = mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshRequestDto)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val responseDto = objectMapper.readValue(response, AuthResponseDto::class.java)
        assertEquals(loginRequest.username, responseDto.username)
        assertEquals("base_test@example.com", responseDto.email)
    }

    @Test
    fun `when given expired refresh token, should refresh token failed`() {
        val refreshRequestDto =
            RefreshRequestDto("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyIDEiLCJpYXQiOjE3NTI5OTk5NzYsImV4cCI6MTc1MzA4NjM3Nn0.uU3-bBJPjYJu46kKNaIhjIAcm8uGPq7eAG4Rzl_V5cOfENom9279KHsx6zYXhPJk")

        val response = mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshRequestDto)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - assertions
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)
        assertEquals(400, errorResponse.status)
        assertEquals("Bad Request", errorResponse.error)
        assertEquals("Refresh token has been expired!", errorResponse.message)
    }
}
