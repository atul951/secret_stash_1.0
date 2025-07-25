package com.secretstash.note.repository

import com.secretstash.note.entity.Note
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NoteRepository : JpaRepository<Note, Long> {

    @Query("""
        SELECT n FROM Note n 
        WHERE n.user.username = :username 
        AND (n.expiresAt IS NULL OR n.expiresAt > :expiry)
        ORDER BY n.createdAt ASC
    """)
    fun findActiveNotesByUsername(@Param("username") username: String, @Param("expiry") expiry: LocalDateTime, pageable: Pageable): List<Note>

    @Query("""
        SELECT n FROM Note n 
        WHERE n.user.username = :username 
        AND (n.expiresAt IS NULL OR n.expiresAt > :expiry)
        ORDER BY n.createdAt DESC
    """)
    fun findLatestActiveNotesByUsername(@Param("username") username: String, @Param("expiry") expiry: LocalDateTime, pageable: Pageable): List<Note>

    @Query("""
        SELECT n FROM Note n 
        WHERE n.user.username = :username 
        AND n.id = :noteId
    """)
    fun findNoteByIdAndUsername(@Param("noteId") noteId: Long, @Param("username") username: String): Note?
}
