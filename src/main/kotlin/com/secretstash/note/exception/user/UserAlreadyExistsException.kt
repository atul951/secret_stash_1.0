package com.secretstash.note.exception.user

class UserAlreadyExistsException(
        private val userId: String?,
        override val message: String
) : RuntimeException(message) {

    constructor(userId: String?) : this(userId, "User '$userId' already exists.")

    override fun toString(): String {
        return if (userId != null) {
            "UserAlreadyExistsException(userId='$userId', message='$message')"
        } else {
            "UserAlreadyExistsException(message='$message')"
        }
    }
}
