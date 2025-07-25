package com.secretstash.note.service

import com.secretstash.note.dto.auth.UserRegistrationRequestDto
import com.secretstash.note.dto.user.UserResponseDto
import com.secretstash.note.entity.User
import com.secretstash.note.exception.user.UserAlreadyExistsException
import com.secretstash.note.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    /**
     * Add new user into the database.
     * */
    fun addNewUser(request: UserRegistrationRequestDto): UserResponseDto {
        userRepository
            .takeIf { repository -> repository.existsByUsername(request.username) }
            ?.let { throw UserAlreadyExistsException(request.username) }

        userRepository
            .takeIf { repository -> repository.existsByEmail(request.email) }
            ?.let { throw UserAlreadyExistsException(request.email) }

        val encodedPassword = passwordEncoder.encode(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = encodedPassword
        )

        return userRepository
            .save(user)
            .toUserDto()
    }

    /**
     * Find a user by username.
     * */
    fun getByUsername(username: String): UserResponseDto {
        return findByUsername(username)
            .toUserDto()
    }

    /**
     * Find a user by username within database.
     * */
    fun findByUsername(username: String): User {
        return userRepository
            .findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User Not found $username") }
    }

    /**
     * Map User object to UserDto class
     * */
    private fun User.toUserDto(): UserResponseDto {
        return UserResponseDto(
            username = this.username,
            email = this.email
        )
    }
}
