package com.secretstash.note.service

import com.secretstash.note.dto.note.CreateNoteRequestDto
import com.secretstash.note.dto.note.NoteResponseDto
import com.secretstash.note.dto.note.UpdateNoteRequestDto
import com.secretstash.note.entity.Note
import com.secretstash.note.exception.note.NoteHasExpiredException
import com.secretstash.note.exception.note.NoteNotFoundException
import com.secretstash.note.repository.NoteRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NoteService(
    private val authService: AuthService,
    private val noteRepository: NoteRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun createNote(request: CreateNoteRequestDto): NoteResponseDto {
        val currentUser = authService.getCurrentUser()
        logger.info { "Creating Note for user=${currentUser.username}" }

        val note = Note(
            title = request.title,
            content = request.content,
            user = currentUser,
            expiresAt = request.expiresAt
        )

        val savedNote = noteRepository.save(note)
        logger.info { "Created Note with ID=${savedNote.id}" }
        return mapToNoteResponse(savedNote)
    }

    fun getNoteById(noteId: Long): NoteResponseDto {
        val currentUsername = authService.getCurrentUsername()
        logger.info { "Fetching Note with ID=$noteId for user=$currentUsername" }
        val note = getActiveNoteByIdAndUsername(noteId, currentUsername)

        return mapToNoteResponse(note)
    }

    fun getAllActiveNotes(page: Int = 0, size: Int = 20): List<NoteResponseDto> {
        val currentUsername = authService.getCurrentUsername()
        logger.info { "Fetching all active Notes for user=$currentUsername" }
        val now = LocalDateTime.now()
        val pageable = PageRequest.of(page, size)

        val notes = noteRepository.findActiveNotesByUsername(currentUsername, now, pageable)
        logger.info { "Fetched ${notes.size} active Notes" }
        return notes.map { mapToNoteResponse(it) }
    }

    fun getLatestActiveNotes(limit: Int = 1000): List<NoteResponseDto> {
        val currentUsername = authService.getCurrentUsername()
        logger.info { "Fetching $limit latest active Notes for user=$currentUsername" }
        val now = LocalDateTime.now()
        val pageable = PageRequest.of(0, limit)

        val notes = noteRepository.findLatestActiveNotesByUsername(currentUsername, now, pageable)
        logger.info { "Fetched ${notes.size} latest active Notes" }
        return notes.map { mapToNoteResponse(it) }
    }

    fun updateNote(noteId: Long, request: UpdateNoteRequestDto): NoteResponseDto {
        val currentUsername = authService.getCurrentUsername()
        logger.info { "Updating Note with ID=$noteId for user=$currentUsername" }

        val existingNote = getActiveNoteByIdAndUsername(noteId, currentUsername)

        existingNote.title = request.title
        existingNote.content = request.content
        existingNote.expiresAt = request.expiresAt

        val savedNote = noteRepository.save(existingNote)
        logger.info { "Updated Note with ID=${savedNote.id}" }
        return mapToNoteResponse(savedNote)
    }

    fun deleteNote(noteId: Long) {
        val currentUsername = authService.getCurrentUsername()
        logger.info { "Deleting Note with ID=$noteId for user=$currentUsername" }

        val note = noteRepository.findNoteByIdAndUsername(noteId, currentUsername)
            ?: throw NoteNotFoundException("Note not found with ID=$noteId.")

        noteRepository.delete(note)
        logger.info { "Deleted Note with ID=${note.id}" }
    }

    private fun getActiveNoteByIdAndUsername(noteId: Long, username: String): Note {
        val now = LocalDateTime.now()

        val note = noteRepository.findNoteByIdAndUsername(noteId, username)
            ?: throw NoteNotFoundException("Note not found with ID=$noteId.")

        if (note.expiresAt?.isAfter(now) == false) {
            throw NoteHasExpiredException(noteId = noteId)
        }

        return note
    }

    private fun mapToNoteResponse(note: Note): NoteResponseDto {
        return NoteResponseDto(
            id = note.id!!,
            title = note.title,
            content = note.content,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            expiresAt = note.expiresAt
        )
    }
}
