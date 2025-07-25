package com.secretstash.note.service

import com.secretstash.note.dto.auth.AuthRequestDto
import com.secretstash.note.dto.auth.UserRegistrationRequestDto
import com.secretstash.note.dto.user.UserResponseDto
import com.secretstash.note.entity.User
import com.secretstash.note.exception.auth.AuthTokenExpireException
import com.secretstash.note.exception.user.UserAlreadyExistsException
import com.secretstash.note.modal.UserTestModal
import com.secretstash.note.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

class AuthServiceTest {
    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var jwtTokenService: JwtTokenService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        jwtTokenService = mock(JwtTokenService::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        authenticationManager = mock(AuthenticationManager::class.java)
        userService = UserService(userRepository, passwordEncoder)
        authService = AuthService(userService, jwtTokenService, authenticationManager)
    }

    @Test
    fun `register should succeed for new user`() {
        //arrange
        val userRegistrationRequestDto = UserRegistrationRequestDto(
            username = "user_1",
            email = "user_1@gmail.com",
            password = "user@123"
        )
        val userResponse = UserResponseDto(username = "user_1", email = "user_1@gmail.com")
        `when`(passwordEncoder.encode("user@123")).thenReturn("encoded_password")

        val user = User(username = "user_1", email = "user_1@gmail.com", password = "encoded_password")
        `when`(userRepository.save(any(User::class.java))).thenReturn(user)

        `when`(userService.addNewUser(userRegistrationRequestDto)).thenReturn(userResponse)

        // act
        val response = authService.register(userRegistrationRequestDto)

        // assert
        assertEquals("user_1", response.username)
        assertEquals("user_1@gmail.com", response.email)
    }

    @Test
    fun `register should fail if username exists`() {
        val request = UserRegistrationRequestDto(username = "user_1", email = "user_1@gmail.com", password = "user@123")
        `when`(userRepository.existsByUsername("user_1")).thenReturn(true)

        val exception = Assertions.assertThrows(UserAlreadyExistsException::class.java) {
            authService.register(request)
        }

        assertEquals("User 'user_1' already exists.", exception.message)
    }

    @Test
    fun `register should fail if email exists`() {
        val request = UserRegistrationRequestDto(username = "user_1", email = "user_1@gmail.com", password = "user@123")
        `when`(userRepository.existsByUsername("user_1")).thenReturn(false)
        `when`(userRepository.existsByEmail("user_1@gmail.com")).thenReturn(true)

        val exception = Assertions.assertThrows(UserAlreadyExistsException::class.java) {
            authService.register(request)
        }

        assertEquals("User 'user_1@gmail.com' already exists.", exception.message)
    }

    @Test
    fun `login should succeed with valid credentials`() {
        val request = AuthRequestDto("user_1", "password")
        val authentication = mock(Authentication::class.java)
        val userDetails = UserTestModal() as UserDetails
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(authentication.principal).thenReturn(userDetails)
        `when`(jwtTokenService.generateToken("user_1")).thenReturn("token")
        `when`(jwtTokenService.generateRefreshToken("user_1")).thenReturn("refreshToken")
        val user = User("user_1", "user_1@gmail.com", "password", LocalDateTime.now())
        `when`(userRepository.findByUsername("user_1")).thenReturn(Optional.of(user))

        val response = authService.login(request)
        assertEquals("token", response.token)
        assertEquals("refreshToken", response.refreshToken)
        assertEquals("user_1", response.username)
        assertEquals("user_1@gmail.com", response.email)
    }

    @Test
    fun `login should fail with invalid credentials`() {
        val request = AuthRequestDto("user_1", "password")
        val authentication = mock(Authentication::class.java)
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenThrow(BadCredentialsException::class.java)

        val exception = Assertions.assertThrows(BadCredentialsException::class.java) {
            authService.login(request)
        }

        assertEquals("Invalid username or password", exception.message)
    }

    @Test
    fun `refreshToken should succeed with valid refresh token`() {
        val refreshToken = "refreshToken"
        `when`(jwtTokenService.extractUsername(refreshToken)).thenReturn("user_1")
        `when`(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false)
        val user = User("user_1", "user_1@email.com", "encoded", LocalDateTime.now())
        `when`(userRepository.findByUsername("user_1")).thenReturn(Optional.of(user))
        `when`(jwtTokenService.generateToken("user_1")).thenReturn("token")

        val response = authService.refreshAccessToken(refreshToken)
        assertEquals("token", response.token)
        assertEquals("refreshToken", response.refreshToken)
        assertEquals("user_1", response.username)
        assertEquals("user_1@email.com", response.email)
    }

    @Test
    fun `refreshToken should fail if token expired`() {
        val refreshToken = "refreshToken"
        `when`(jwtTokenService.extractUsername(refreshToken)).thenReturn("user_1")
        `when`(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(true)

        val exception = assertThrows(AuthTokenExpireException::class.java) {
            authService.refreshAccessToken(refreshToken)
        }

        assertEquals("Refresh token has been expired!", exception.message)
    }

    @Test
    fun `refreshToken should fail if user not found`() {
        val refreshToken = "refreshToken"
        `when`(jwtTokenService.extractUsername(refreshToken)).thenReturn("user_1")
        `when`(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false)
        `when`(userRepository.findByUsername("user_1")).thenReturn(Optional.empty())

        val exception = Assertions.assertThrows(UsernameNotFoundException::class.java) {
            authService.refreshAccessToken(refreshToken)
        }

        assertEquals("User Not found user_1", exception.message)
    }
}
