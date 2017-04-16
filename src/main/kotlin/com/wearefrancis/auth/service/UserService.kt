package com.wearefrancis.auth.service

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.dto.userModelToReadUserByAdminDTO
import com.wearefrancis.auth.dto.userModelToReadUserByOwnerDTO
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
        private val passwordEncoder: BCryptPasswordEncoder,
        private val userRepository: UserRepository
) {
    fun create(userDTO: WriteUserDTO, byAdmin: Boolean = false): ReadUserDTO {
        if (userRepository.existsByUsername(userDTO.username!!)) {
            throw ObjectAlreadyExistsException("Username ${userDTO.username} already used")
        }
        if (userRepository.existsByEmail(userDTO.email!!)) {
            throw ObjectAlreadyExistsException("Email ${userDTO.email} already used")
        }
        return when (byAdmin) {
            true -> userModelToReadUserByAdminDTO(userRepository.save(User(
                    email = userDTO.email,
                    enabled = true,
                    password = passwordEncoder.encode(userDTO.password),
                    username = userDTO.username
            )))
            false -> userModelToReadUserByOwnerDTO(userRepository.save(User(
                    email = userDTO.email,
                    password = passwordEncoder.encode(userDTO.password),
                    username = userDTO.username
            )))
        }
    }
}