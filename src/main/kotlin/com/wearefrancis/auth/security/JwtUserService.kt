package com.wearefrancis.auth.security

import com.wearefrancis.auth.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
open class JwtUserService(val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = userRepository.findByUsername(username!!)
            ?: throw UsernameNotFoundException("User $username not found")
}