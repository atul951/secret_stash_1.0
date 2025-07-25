package com.secretstash.note.dto.auth

import jakarta.validation.constraints.NotBlank

data class AuthRequestDto(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String,
)
