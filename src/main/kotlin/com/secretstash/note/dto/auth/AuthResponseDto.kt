package com.secretstash.note.dto.auth

data class AuthResponseDto(
    val token: String,
    val type: String = "Bearer",
    val username: String,
    val email: String,
    val refreshToken: String
)
