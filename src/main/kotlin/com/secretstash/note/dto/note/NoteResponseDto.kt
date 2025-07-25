package com.secretstash.note.dto.note

import java.time.LocalDateTime

data class NoteResponseDto(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val expiresAt: LocalDateTime?
)
