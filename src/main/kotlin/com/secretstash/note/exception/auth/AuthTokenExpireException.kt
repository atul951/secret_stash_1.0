package com.secretstash.note.exception.auth

class AuthTokenExpireException(override val message: String): RuntimeException(message)
