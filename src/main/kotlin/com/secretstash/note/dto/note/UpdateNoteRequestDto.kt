package com.secretstash.note.dto.note

import com.secretstash.note.dto.validator.FutureDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateNoteRequestDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters.")
    val title: String,

    @field:NotBlank(message = "Content is required")
    @field:Size(max = 10000, message = "Content must not exceed 10000 characters.")
    val content: String,

    @field:FutureDateTime(message = "Expiration date and time must be in the future.")
    val expiresAt: LocalDateTime? = null
)
