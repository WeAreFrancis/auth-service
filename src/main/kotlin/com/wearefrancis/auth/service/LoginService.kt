package com.wearefrancis.auth.service

import com.wearefrancis.auth.dto.JwtDTO
import com.wearefrancis.auth.exception.BadCredentialsException
import com.wearefrancis.auth.repository.UserRepository
import com.wearefrancis.auth.security.JwtUtils
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginService(
        val jwtUtils: JwtUtils,
        val passwordEncoder: BCryptPasswordEncoder,
        val userRepository: UserRepository
) {
    companion object {
        val logger = LoggerFactory.getLogger(LoginService::class.java)!!
    }

    fun login(username: String, password: String): JwtDTO {
        val user = userRepository.findByUsername(username)
        if (user == null || !passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException()
        }
        val jwt = JwtDTO(jwtUtils.generateJwt(user))
        logger.info("JWT for $username generated")
        return jwt
    }
}