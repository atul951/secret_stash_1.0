package com.secretstash.note.exception.note

class NoteHasExpiredException(
        private val noteId: Long?,
        override val message: String
) : RuntimeException(message) {

    constructor(noteId: Long?) : this(noteId, "Note '$noteId' has already expired.")

    override fun toString(): String {
        return if (noteId != null) {
            "NoteHasExpiredException(noteId='$noteId', message='$message')"
        } else {
            "NoteHasExpiredException(message='$message')"
        }
    }
}
