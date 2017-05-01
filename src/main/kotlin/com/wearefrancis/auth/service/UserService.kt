package com.wearefrancis.auth.service

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.CreateUserDTO
import com.wearefrancis.auth.dto.ReadUserByAdminDTO
import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.UpdateUserDTO
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByUserDTOMapper
import com.wearefrancis.auth.exception.EntityNotFoundException
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.io.Serializable
import java.util.*

@Service
class UserService(
        private val passwordEncoder: BCryptPasswordEncoder,
        private val readUserByAdminDTOMapper: ReadUserByAdminDTOMapper,
        private val readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper,
        private val readUserByUserDTOMapper: ReadUserByUserDTOMapper,
        private val userRepository: UserRepository
) {
    companion object {
        val logger = LoggerFactory.getLogger(UserService::class.java)!!
    }

    fun create(userDTO: CreateUserDTO, byAdmin: Boolean = false): ReadUserDTO {
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
        val userCreated = userRepository.save(user)
        logger.info("User ${userCreated.username} created")
        return when (byAdmin) {
            true -> readUserByAdminDTOMapper.convert(userCreated)
            false -> readUserByOwnerDTOMapper.convert(userCreated)
        }
    }

    fun delete(userId: UUID) {
        if (!userRepository.exists(userId)) {
            throw EntityNotFoundException("User $userId not found")
        }
        userRepository.delete(userId)
        logger.info("User $userId deleted")
    }

    fun enable(userId: UUID): ReadUserByAdminDTO {
        val userToEnable = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        val user = userToEnable.copy(
                enabled = true
        )
        val userEnabled = userRepository.save(user)
        logger.info("User ${userEnabled.username} enabled")
        return readUserByAdminDTOMapper.convert(userEnabled)
    }

    fun getById(userId: UUID, currentUser: User): ReadUserDTO {
        val user = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        return userModelToDTO(userId, user, currentUser)
    }

    fun getByUsername(username: String, currentUser: User): ReadUserDTO {
        val user = userRepository.findByUsername(username) ?: throw EntityNotFoundException("User $username not found")
        return userModelToDTO(username, user, currentUser)
    }

    fun update(userId: UUID, userDTO: UpdateUserDTO, byAdmin: Boolean = false): ReadUserDTO {
        val userToUpdate = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        if (userRepository.existsByEmailAndIdNot(userDTO.email, userId)) {
            throw ObjectAlreadyExistsException("Email ${userDTO.email} already used")
        }
        val user = userToUpdate.copy(
                email = userDTO.email,
                password = passwordEncoder.encode(userDTO.password)
        )
        val userUpdated = userRepository.save(user)
        logger.info("User ${userUpdated.username} updated")
        return when (byAdmin) {
            true -> readUserByAdminDTOMapper.convert(userUpdated)
            false -> readUserByOwnerDTOMapper.convert(userUpdated)
        }
    }

    private fun userModelToDTO(id: Serializable, user: User, currentUser: User): ReadUserDTO = when {
        currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN -> readUserByAdminDTOMapper.convert(user)
        currentUser.id == id || currentUser.username == id -> readUserByOwnerDTOMapper.convert(user)
        else -> readUserByUserDTOMapper.convert(user)
    }
}