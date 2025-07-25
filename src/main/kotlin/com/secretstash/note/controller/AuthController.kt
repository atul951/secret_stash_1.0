package com.secretstash.note.controller

import com.secretstash.note.dto.ErrorResponseDto
import com.secretstash.note.dto.auth.*
import com.secretstash.note.service.AuthService
import com.secretstash.note.service.RateLimitService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Operations related to authentication")
class AuthController(
    private val authService: AuthService,
    private val rateLimitService: RateLimitService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Operation(
        summary = "Register user",
        description = "Registers a new user with given details.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "User details.",
                content = [Content(
                    schema = Schema(implementation = UserRegistrationResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad user input.",
                content = [Content(
                    schema = Schema(implementation = UserRegistrationResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate Limited - Too Many Requests.",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: UserRegistrationRequestDto,
        httpRequest: HttpServletRequest
    ): ResponseEntity<UserRegistrationResponseDto> {
        logger.info { "Received User register request with username=${request.username}" }
        if (rateLimitService.isRateLimitedByIp(httpRequest)) {
            logger.info { "Received too many requests for username=${request.username}" }
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))
    }

    @Operation(
        summary = "User Login",
        description = "Authenticates a user and returns JWT tokens (access and refresh).",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful.",
                content = [Content(
                    schema = Schema(implementation = AuthResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid username or password",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate Limited - Too Many Requests.",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AuthRequestDto,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponseDto> {
        logger.info { "Received User login request with username=${request.username}" }
        if (rateLimitService.isRateLimitedByUserAndIp(request.username, httpRequest)) {
            logger.info { "Received too many requests for username=${request.username}" }
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
        }
        return ResponseEntity.ok(authService.login(request))
    }

    @Operation(
        summary = "Refresh Access Token",
        description = "Uses a refresh token to obtain a new access token and refresh token.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Token refreshed successfully",
                content = [Content(
                    schema = Schema(implementation = AuthResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid refresh token",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequestDto): ResponseEntity<AuthResponseDto> {
        val response = authService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
