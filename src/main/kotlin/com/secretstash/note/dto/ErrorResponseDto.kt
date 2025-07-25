package com.secretstash.note.dto

import java.time.LocalDateTime

data class ErrorResponseDto(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
)
