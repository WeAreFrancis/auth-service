package com.wearefrancis.auth.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserByAdminDTO
import com.wearefrancis.auth.dto.ReadUserByOwnerDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByUserDTOMapper
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class UserServiceTest {
    private lateinit var readUserByAdminDTOMapper: ReadUserByAdminDTOMapper
    private lateinit var readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper
    private lateinit var readUserByUserDTOMapper: ReadUserByUserDTOMapper
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @Before
    fun setUp() {
        readUserByAdminDTOMapper = mock<ReadUserByAdminDTOMapper>()
        readUserByOwnerDTOMapper = mock<ReadUserByOwnerDTOMapper>()
        readUserByUserDTOMapper = mock<ReadUserByUserDTOMapper>()
        userRepository = mock<UserRepository>()
        userService = UserService(
                passwordEncoder = BCryptPasswordEncoder(),
                readUserByAdminDTOMapper = readUserByAdminDTOMapper,
                readUserByOwnerDTOMapper = readUserByOwnerDTOMapper,
                readUserByUserDTOMapper = readUserByUserDTOMapper,
                userRepository = userRepository
        )
    }

    @Test
    fun createShouldThrowObjectAlreadyExistsExceptionIfTheUsernameIsAlreadyUsed() {
        // GIVEN
        val writeUserDTO = WriteUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = writeUserDTO.email,
                username = writeUserDTO.username
        )
        whenever(userRepository.existsByUsername(writeUserDTO.username)).thenReturn(true)
        whenever(userRepository.save(any<User>())).then({
            invocation -> invocation.getArgumentAt(0, User::class.java)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        try {
            // WHEN
            userService.create(writeUserDTO)

            // THEN
            verify(userRepository).existsByUsername(writeUserDTO.username)
            verify(userRepository).existsByEmail(writeUserDTO.email)
            verify(userRepository).save(any<User>())
            verify(readUserByOwnerDTOMapper.convert(any<User>()))
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Username ${writeUserDTO.username} already used")
        }
    }

    @Test
    fun createShouldThrowObjectAlreadyExistsExceptionIfTheEmailIsAlreadyUsed() {
        // GIVEN
        val writeUserDTO = WriteUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = writeUserDTO.email,
                username = writeUserDTO.username
        )
        whenever(userRepository.existsByEmail(writeUserDTO.email)).thenReturn(true)
        whenever(userRepository.save(any<User>())).then({
            invocation -> invocation.getArgumentAt(0, User::class.java)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        try {
            // WHEN
            userService.create(writeUserDTO)

            // THEN
            verify(userRepository).existsByUsername(writeUserDTO.username)
            verify(userRepository).existsByEmail(writeUserDTO.email)
            verify(userRepository).save(any<User>())
            verify(readUserByOwnerDTOMapper.convert(any<User>()))
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Email ${writeUserDTO.email} already used")
        }
    }

    @Test
    fun createShouldReturnReadUserByOwnerDTOIfByAdminIsFalse() {
        // GIVEN
        val writeUserDTO = WriteUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = writeUserDTO.email,
                username = writeUserDTO.username
        )
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsCreatedFromDTOAndReturnIt(invocation.getArgumentAt(0, User::class.java), writeUserDTO)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        // WHEN
        val readUserDTO = userService.create(writeUserDTO)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByOwnerDTO)
        verify(userRepository).existsByUsername(writeUserDTO.username)
        verify(userRepository).existsByEmail(writeUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByOwnerDTOMapper).convert(any<User>())
    }

    @Test
    fun createShouldReturnReadUserByAdminDTOIfByAdminIsTrue() {
        // GIVEN
        val writeUserDTO = WriteUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = writeUserDTO.email,
                enabled = true,
                username = writeUserDTO.username
        )
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsCreatedFromDTOAndReturnIt(invocation.getArgumentAt(0, User::class.java), writeUserDTO, true)
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.create(writeUserDTO, true)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).existsByUsername(writeUserDTO.username)
        verify(userRepository).existsByEmail(writeUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    private fun assertUserIsCreatedFromDTOAndReturnIt(
            user: User, writeUserDTO: WriteUserDTO, byAdmin: Boolean = false
    ): User {
        assertThat(user.email).isEqualTo(writeUserDTO.email)
        assertThat(user.enabled).isEqualTo(byAdmin)
        assertThat(user.role).isEqualTo(User.Role.USER)
        assertThat(user.isAccountNonExpired).isTrue()
        assertThat(user.isAccountNonLocked).isTrue()
        assertThat(user.isCredentialsNonExpired).isTrue()
        assertThat(user.isEnabled).isEqualTo(user.enabled)
        return user
    }
}