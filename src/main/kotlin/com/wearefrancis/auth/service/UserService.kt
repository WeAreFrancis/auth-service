package com.wearefrancis.auth.service

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.*
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByUserDTOMapper
import com.wearefrancis.auth.exception.EntityNotFoundException
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.TokenRepository
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
        private val tokenService: TokenService,
        private val tokenRepository: TokenRepository,
        private val userRepository: UserRepository
) {
    companion object {
        val logger = LoggerFactory.getLogger(UserService::class.java)!!
    }

    fun activate(tokenValue: UUID) {
        val token = tokenRepository.findByValue(tokenValue)
                ?: throw EntityNotFoundException("Token $tokenValue not found")
        val user = userRepository.save(token.user.copy(
                enabled = true
        ))
        logger.info("User ${user.username} activated")
        tokenRepository.delete(token)
        logger.info("Token ${token.value} deleted")
    }

    fun changeRole(userId: UUID, userRoleDTO: WriteUserRoleDTO): ReadUserByAdminDTO {
        val userToUpdate = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        val user = userRepository.save(userToUpdate.copy(
                role = userRoleDTO.role
        ))
        logger.info("User ${user.username} is now ${user.role}")
        return readUserByAdminDTOMapper.convert(user)
    }

    fun create(userDTO: CreateUserDTO, byAdmin: Boolean = false): ReadUserDTO {
        if (userRepository.existsByUsername(userDTO.username)) {
            throw ObjectAlreadyExistsException("Username ${userDTO.username} already used")
        }
        if (userRepository.existsByEmail(userDTO.email)) {
            throw ObjectAlreadyExistsException("Email ${userDTO.email} already used")
        }
        val user = userRepository.save(User(
                email = userDTO.email,
                enabled = byAdmin,
                password = passwordEncoder.encode(userDTO.password),
                username = userDTO.username
        ))
        logger.info("User ${user.username} created")
        return when (byAdmin) {
            true -> readUserByAdminDTOMapper.convert(user)
            false -> {
                tokenService.sendMail(user)
                readUserByOwnerDTOMapper.convert(user)
            }
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
        val userToUpdate = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        val user = userRepository.save(userToUpdate.copy(
                enabled = true
        ))
        logger.info("User ${user.username} enabled")
        return readUserByAdminDTOMapper.convert(user)
    }

    fun getById(userId: UUID, currentUser: User): ReadUserDTO {
        val user = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        return userModelToDTO(userId, user, currentUser)
    }

    fun getByUsername(username: String, currentUser: User): ReadUserDTO {
        val user = userRepository.findByUsername(username) ?: throw EntityNotFoundException("User $username not found")
        return userModelToDTO(username, user, currentUser)
    }

    fun lock(userId: UUID): ReadUserByAdminDTO {
        val userToLock = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        val user = userRepository.save(userToLock.copy(
                locked = true
        ))
        logger.info("User ${user.username} locked")
        return readUserByAdminDTOMapper.convert(user)
    }

    fun unlock(userId: UUID): ReadUserByAdminDTO {
        val userToUnlock = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        val user = userRepository.save(userToUnlock.copy(
                locked = false
        ))
        logger.info("User ${user.username} unlocked")
        return readUserByAdminDTOMapper.convert(user)
    }

    fun update(userId: UUID, userDTO: UpdateUserDTO, byAdmin: Boolean = false): ReadUserDTO {
        val userToUpdate = userRepository.findOne(userId) ?: throw EntityNotFoundException("User $userId not found")
        if (userRepository.existsByEmailAndIdNot(userDTO.email, userId)) {
            throw ObjectAlreadyExistsException("Email ${userDTO.email} already used")
        }
        val user = userRepository.save(userToUpdate.copy(
                email = userDTO.email,
                password = passwordEncoder.encode(userDTO.password)
        ))
        logger.info("User ${user.username} updated")
        return when (byAdmin) {
            true -> readUserByAdminDTOMapper.convert(user)
            false -> readUserByOwnerDTOMapper.convert(user)
        }
    }

    private fun userModelToDTO(id: Serializable, user: User, currentUser: User): ReadUserDTO = when {
        currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN -> readUserByAdminDTOMapper.convert(user)
        currentUser.id == id || currentUser.username == id -> readUserByOwnerDTOMapper.convert(user)
        else -> readUserByUserDTOMapper.convert(user)
    }
}