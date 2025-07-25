package com.secretstash.note.controller

import com.secretstash.note.dto.user.UserResponseDto
import com.secretstash.note.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Users", description = "Operations related to user")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required!",
            content = [Content(schema = Schema(hidden = true))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions or invalid token.",
            content = [Content(schema = Schema(hidden = true))]
        )
    ]
)
class UserController(private val userService: UserService) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Operation(
        summary = "Get the user details",
        description = "Retrieves the current logged in user's details.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "User details.",
                content = [Content(
                    schema = Schema(implementation = UserResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @GetMapping("/api/user")
    fun getUser(@AuthenticationPrincipal currentUser: UserDetails): UserResponseDto {
        logger.info { "Fetching user details for user=${currentUser.username}" }
        return userService.getByUsername(currentUser.username)
    }
}
