package com.secretstash.note.service

import com.secretstash.note.repository.UserRepository
import org.springframework.context.annotation.Primary
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

typealias CurrentUser = com.secretstash.note.entity.User

@Service
@Primary
class CustomUserDetailService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository
            .findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User $username Not Found!") }
            .toUserDetails()
    }

    private fun CurrentUser.toUserDetails(): UserDetails {
        return User
            .builder()
            .username(this.username)
            .password(this.password)
            .build()
    }
}
