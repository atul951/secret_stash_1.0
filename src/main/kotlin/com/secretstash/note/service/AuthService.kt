package com.secretstash.note.service

import com.secretstash.note.dto.auth.AuthRequestDto
import com.secretstash.note.dto.auth.UserRegistrationRequestDto
import com.secretstash.note.dto.auth.UserRegistrationResponseDto
import com.secretstash.note.dto.auth.AuthResponseDto
import com.secretstash.note.entity.User
import com.secretstash.note.exception.auth.AuthTokenExpireException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val jwtTokenService: JwtTokenService,
    private val authenticationManager: AuthenticationManager,
) {
    /**
     * Register a new user into the system.
     * */
    fun register(request: UserRegistrationRequestDto): UserRegistrationResponseDto {
        val newUser = userService.addNewUser(request)

        return UserRegistrationResponseDto(
            username = newUser.username,
            email = newUser.email
        )
    }

    /**
     * User log in.
     * */
    fun login(request: AuthRequestDto): AuthResponseDto {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            val userDetails = authentication.principal as UserDetails
            val token = jwtTokenService.generateToken(userDetails.username)
            val refreshToken = jwtTokenService.generateRefreshToken(userDetails.username)

            val user = userService.findByUsername(request.username)
            return AuthResponseDto(
                token = token,
                username = user.username,
                email = user.email,
                refreshToken = refreshToken,
            )
        } catch (e: BadCredentialsException) {
            throw BadCredentialsException("Invalid username or password")
        } catch (e: Exception) {
            throw java.lang.Exception(e)
        }
    }

    /**
     * Fetch a new access token using refresh token.
     * */
    fun refreshAccessToken(refreshToken: String): AuthResponseDto {
        if (jwtTokenService.isTokenExpired(refreshToken)) {
            throw AuthTokenExpireException("Refresh token has been expired!")
        }

        val username = jwtTokenService.extractUsername(refreshToken)
        val user = userService.findByUsername(username)
        val newToken = jwtTokenService.generateToken(user.username)
        return AuthResponseDto(
            token = newToken,
            username = user.username,
            email = user.email,
            refreshToken = refreshToken
        )
    }

    /**
     * Fetch current username using security context.
     */
    fun getCurrentUsername(): String {
        return SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("User not authenticated")
    }

    /**
     * Fetch current username using security context.
     */
    fun getCurrentUser(): User {
        val username = getCurrentUsername()
        return userService.findByUsername(username)
    }
}
