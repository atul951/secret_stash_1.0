package com.secretstash.note.controller

import com.secretstash.note.dto.ErrorResponseDto
import com.secretstash.note.dto.note.CreateNoteRequestDto
import com.secretstash.note.dto.note.NoteResponseDto
import com.secretstash.note.dto.note.UpdateNoteRequestDto
import com.secretstash.note.service.NoteService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "Operations related to user notes")
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
class NoteController(
    private val noteService: NoteService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Operation(
        summary = "Create a new note",
        description = "Allows authenticated users to create a new personal note.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Note created successfully.",
                content = [Content(
                    schema = Schema(implementation = NoteResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input or note content exceeds limit.",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @PostMapping
    fun createNote(@Valid @RequestBody request: CreateNoteRequestDto): ResponseEntity<NoteResponseDto> {
        logger.info { "Received Note creation request" }
        val note = noteService.createNote(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(note)
    }

    @Operation(
        summary = "Get a note by ID",
        description = "Retrieves a specific note by its ID, accessible only to the note's owner.",
        parameters = [
            Parameter(name = "noteId", description = "ID of the note to retrieve.", required = true, example = "123")
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Note with note ID.",
                content = [Content(
                    schema = Schema(implementation = NoteResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Note has expired!",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Note not found or you don't have access!",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @GetMapping("/{noteId}")
    fun getNoteById(@PathVariable noteId: Long): ResponseEntity<NoteResponseDto> {
        val note = noteService.getNoteById(noteId)
        return ResponseEntity.ok(note)
    }

    @Operation(
        summary = "Retrieve all active notes",
        description = "Fetches a paginated list of all active notes accessible to the authenticated user.",
        parameters = [
            Parameter(
                name = "page",
                description = "Page number (0-indexed) for pagination",
                schema = Schema(type = "integer", format = "int32", defaultValue = "0")
            ),
            Parameter(
                name = "size",
                description = "Number of notes per page",
                schema = Schema(type = "integer", format = "int32", defaultValue = "20")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user's active notes.",
                content = [Content(
                    schema = Schema(implementation = Array<NoteResponseDto>::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @GetMapping
    fun getAllNotes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<NoteResponseDto>> {
        val notes = noteService.getAllActiveNotes(page, size)
        return ResponseEntity.ok(notes)
    }

    @Operation(
        summary = "Retrieve latest notes",
        description = "Fetches a specified number of the most recently created active notes.",
        parameters = [
            Parameter(
                name = "limit",
                description = "Maximum number of latest notes to retrieve.",
                schema = Schema(type = "integer", format = "int32", defaultValue = "1000")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user's latest notes.",
                content = [Content(
                    schema = Schema(implementation = Array<NoteResponseDto>::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @GetMapping("/latest")
    fun getLatestNotes(@RequestParam(defaultValue = "1000") limit: Int): ResponseEntity<List<NoteResponseDto>> {
        val notes = noteService.getLatestActiveNotes(limit)
        return ResponseEntity.ok(notes)
    }

    @Operation(
        summary = "Update a note by ID",
        description = "Updates a specific note by its ID, accessible only to the note's owner.",
        parameters = [
            Parameter(name = "noteId", description = "ID of the note to update.", required = true, example = "123")
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Note with note ID has been updated.",
                content = [Content(
                    schema = Schema(implementation = NoteResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input or Note has expired!",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Note not found or you don't have access!",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @PutMapping("/{noteId}")
    fun updateNote(
        @PathVariable noteId: Long,
        @Valid @RequestBody request: UpdateNoteRequestDto
    ): ResponseEntity<NoteResponseDto> {
        logger.info { "Received Note update request" }
        val note = noteService.updateNote(noteId, request)
        return ResponseEntity.ok(note)
    }

    @Operation(
        summary = "Delete a note by ID",
        description = "Deletes a specific note by its ID, accessible only to the note's owner.",
        parameters = [
            Parameter(name = "noteId", description = "ID of the note to delete.", required = true, example = "123")
        ],
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Note with note ID has been deleted."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Note not found or you don't have access!",
                content = [Content(
                    schema = Schema(implementation = ErrorResponseDto::class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )]
            )
        ]
    )
    @DeleteMapping("/{noteId}")
    fun deleteNote(@PathVariable noteId: Long): ResponseEntity<Void> {
        logger.info { "Received Note deletion request" }
        noteService.deleteNote(noteId)
        return ResponseEntity.noContent().build()
    }
}
