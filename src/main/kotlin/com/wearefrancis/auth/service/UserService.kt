package com.wearefrancis.auth.service

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
        private val passwordEncoder: BCryptPasswordEncoder,
        private val readUserByAdminDTOMapper: ReadUserByAdminDTOMapper,
        private val readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper,
        private val userRepository: UserRepository
) {
    companion object {
        val logger = LoggerFactory.getLogger(UserService::class.java)!!
    }

    fun create(userDTO: WriteUserDTO, byAdmin: Boolean = false): ReadUserDTO {
        if (userRepository.existsByUsername(userDTO.username)) {
            throw ObjectAlreadyExistsException("Username ${userDTO.username} already used")
        }
        if (userRepository.existsByEmail(userDTO.email)) {
            throw ObjectAlreadyExistsException("Email ${userDTO.email} already used")
        }
        val user = User(
                email = userDTO.email,
                enabled = byAdmin,
                password = passwordEncoder.encode(userDTO.password),
                username = userDTO.username
        )
        logger.info("User ${user.username} created")
        return when (byAdmin) {
            true -> readUserByAdminDTOMapper.convert(userRepository.save(user))
            false -> readUserByOwnerDTOMapper.convert(userRepository.save(user))
        }
    }
}