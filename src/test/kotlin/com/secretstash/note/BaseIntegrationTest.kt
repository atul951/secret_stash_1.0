package com.secretstash.note

import com.fasterxml.jackson.databind.ObjectMapper
import com.secretstash.note.dto.auth.AuthRequestDto
import com.secretstash.note.dto.auth.AuthResponseDto
import com.secretstash.note.entity.User
import com.secretstash.note.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseIntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    protected var username: String = "base_test_user"
    protected var authToken: String = ""

    @BeforeEach
    fun addUser() {
        // create user
        val passwordEncoder = BCryptPasswordEncoder()
        val plainPassword = "password123"
        val hashedPassword = passwordEncoder.encode(plainPassword)
        val user = User(
            username = this.username,
            email = "base_test@example.com",
            password = hashedPassword
        )
        userRepository.save(user)

        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AuthRequestDto(
                    username = username,
                    password = plainPassword
                )
            )
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - fetch token
        val authResponse = objectMapper.readValue(response, AuthResponseDto::class.java)

        // Assign the token to global variable
        this.authToken = authResponse.token
    }

    @AfterEach
    fun deleteUser() {
        userRepository.deleteById(this.username)
    }
}
