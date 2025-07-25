package com.secretstash.note.modal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserTestModal : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }

    override fun getPassword(): String {
        return "password"
    }

    override fun getUsername(): String {
        return "user_1"
    }
}
