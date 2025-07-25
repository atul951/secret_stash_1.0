package com.secretstash.note.exception

import com.secretstash.note.dto.ErrorResponseDto
import com.secretstash.note.exception.auth.AuthTokenExpireException
import com.secretstash.note.exception.note.NoteHasExpiredException
import com.secretstash.note.exception.note.NoteNotFoundException
import com.secretstash.note.exception.user.UserAlreadyExistsException
import com.secretstash.note.exception.user.UserNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = "Invalid username or password",
        )
        logger.error { errorResponse }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }.joinToString(", ")

        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
        )
        logger.error { errorResponse }
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(value = [UserNotFoundException::class, NoteNotFoundException::class])
    fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found"
        )
        logger.error { errorResponse }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(value = [UserAlreadyExistsException::class, NoteHasExpiredException::class])
    fun handleBadRequestExceptions(ex: RuntimeException): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid Request"
        )
        logger.error { errorResponse }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(value = [AuthTokenExpireException::class])
    fun handleAuthTokenExpireException(ex: RuntimeException): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid Request"
        )
        logger.error { errorResponse }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. ${ex.message}",
        )
        logger.error { errorResponse }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
