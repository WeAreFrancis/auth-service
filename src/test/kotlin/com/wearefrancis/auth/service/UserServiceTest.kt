package com.wearefrancis.auth.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.*
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByUserDTOMapper
import com.wearefrancis.auth.exception.EntityNotFoundException
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

class UserServiceTest {
    companion object {
        val passwordEncoder = BCryptPasswordEncoder()
    }

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
                passwordEncoder = passwordEncoder,
                readUserByAdminDTOMapper = readUserByAdminDTOMapper,
                readUserByOwnerDTOMapper = readUserByOwnerDTOMapper,
                readUserByUserDTOMapper = readUserByUserDTOMapper,
                userRepository = userRepository
        )
    }

    @Test
    fun createShouldThrowObjectAlreadyExistsExceptionIfTheUsernameIsAlreadyUsed() {
        // GIVEN
        val createUserDTO = CreateUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = createUserDTO.email,
                username = createUserDTO.username
        )
        whenever(userRepository.existsByUsername(createUserDTO.username)).thenReturn(true)
        whenever(userRepository.save(any<User>())).then({
            invocation -> invocation.getArgumentAt(0, User::class.java)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        try {
            // WHEN
            userService.create(createUserDTO)

            // THEN
            verify(userRepository).existsByUsername(createUserDTO.username)
            verify(userRepository).existsByEmail(createUserDTO.email)
            verify(userRepository).save(any<User>())
            verify(readUserByOwnerDTOMapper.convert(any<User>()))
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Username ${createUserDTO.username} already used")
        }
    }

    @Test
    fun createShouldThrowObjectAlreadyExistsExceptionIfTheEmailIsAlreadyUsed() {
        // GIVEN
        val createUserDTO = CreateUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = createUserDTO.email,
                username = createUserDTO.username
        )
        whenever(userRepository.existsByEmail(createUserDTO.email)).thenReturn(true)
        whenever(userRepository.save(any<User>())).then({
            invocation -> invocation.getArgumentAt(0, User::class.java)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        try {
            // WHEN
            userService.create(createUserDTO)

            // THEN
            verify(userRepository).existsByUsername(createUserDTO.username)
            verify(userRepository).existsByEmail(createUserDTO.email)
            verify(userRepository).save(any<User>())
            verify(readUserByOwnerDTOMapper.convert(any<User>()))
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Email ${createUserDTO.email} already used")
        }
    }

    @Test
    fun createShouldReturnReadUserByOwnerDTOIfByAdminIsFalse() {
        // GIVEN
        val createUserDTO = CreateUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = createUserDTO.email,
                username = createUserDTO.username
        )
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsCreatedFromDTOAndReturnIt(invocation.getArgumentAt(0, User::class.java), createUserDTO)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        // WHEN
        val readUserDTO = userService.create(createUserDTO)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByOwnerDTO)
        verify(userRepository).existsByUsername(createUserDTO.username)
        verify(userRepository).existsByEmail(createUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByOwnerDTOMapper).convert(any<User>())
    }

    @Test
    fun createShouldReturnReadUserByAdminDTOIfByAdminIsTrue() {
        // GIVEN
        val createUserDTO = CreateUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = createUserDTO.email,
                enabled = true,
                username = createUserDTO.username
        )
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsCreatedFromDTOAndReturnIt(invocation.getArgumentAt(0, User::class.java), createUserDTO, true)
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.create(createUserDTO, true)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).existsByUsername(createUserDTO.username)
        verify(userRepository).existsByEmail(createUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    @Test
    fun getByIdShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val id = UUID.randomUUID()
        val currentUser = User()

        try {
            // WHEN
            userService.getById(id, currentUser)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $id not found")
            verify(userRepository).findOne(id)
        }
    }

    @Test
    fun getByIdShouldReturnReadUserByUserDTOIfCurrentUserIsUser() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val currentUser = User()
        val readUserByUserDTO = ReadUserByUserDTO(
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(readUserByUserDTOMapper.convert(user)).thenReturn(readUserByUserDTO)

        // WHEN
        val readUserDTO = userService.getById(user.id, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByUserDTO)
        verify(userRepository).findOne(user.id)
        verify(readUserByUserDTOMapper).convert(user)
    }

    @Test
    fun getByIdShouldReturnReadUserByOwnerDTOIfCurrentUserHasTheGivenId() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val currentUser = User(
                id = user.id
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = user.email,
                id = user.id,
                role = user.role,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(readUserByOwnerDTOMapper.convert(user)).thenReturn(readUserByOwnerDTO)

        // WHEN
        val readUserDTO = userService.getById(user.id, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByOwnerDTO)
        verify(userRepository).findOne(user.id)
        verify(readUserByOwnerDTOMapper).convert(user)
    }

    @Test
    fun getByIdShouldReturnReadUserByAdminDTOIfCurrentUserIsAdmin() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val currentUser = User(
                role = User.Role.ADMIN
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(readUserByAdminDTOMapper.convert(user)).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.getById(user.id, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(readUserByAdminDTOMapper).convert(user)
    }

    @Test
    fun getByIdShouldReturnReadUserByAdminDTOIfCurrentUserIsSuperAdmin() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val currentUser = User(
                role = User.Role.SUPER_ADMIN
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(readUserByAdminDTOMapper.convert(user)).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.getById(user.id, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(readUserByAdminDTOMapper).convert(user)
    }

    @Test
    fun getByUsernameShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenUsernameIsNotFound() {
        // GIVEN
        val username = "gleroy"
        val currentUser = User()

        try {
            // WHEN
            userService.getByUsername(username, currentUser)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $username not found")
            verify(userRepository).findByUsername(username)
        }
    }

    @Test
    fun getByUsernameShouldReturnReadUserByUserDTOIfCurrentUserIsUser() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        val currentUser = User()
        val readUserByUserDTO = ReadUserByUserDTO(
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(readUserByUserDTOMapper.convert(user)).thenReturn(readUserByUserDTO)

        // WHEN
        val readUserDTO = userService.getByUsername(user.username, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByUserDTO)
        verify(userRepository).findByUsername(user.username)
        verify(readUserByUserDTOMapper).convert(user)
    }

    @Test
    fun getByUsernameShouldReturnReadUserByOwnerDTOIfCurrentUserHasTheGivenUsername() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        val currentUser = User(
                username = user.username
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = user.email,
                id = user.id,
                role = user.role,
                username = user.username
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(readUserByOwnerDTOMapper.convert(user)).thenReturn(readUserByOwnerDTO)

        // WHEN
        val readUserDTO = userService.getByUsername(user.username, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByOwnerDTO)
        verify(userRepository).findByUsername(user.username)
        verify(readUserByOwnerDTOMapper).convert(user)
    }

    @Test
    fun getByUsernameShouldReturnReadUserByAdminDTOIfCurrentUserIsAdmin() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        val currentUser = User(
                role = User.Role.ADMIN
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(readUserByAdminDTOMapper.convert(user)).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.getByUsername(user.username, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findByUsername(user.username)
        verify(readUserByAdminDTOMapper).convert(user)
    }

    @Test
    fun getByUsernameShouldReturnReadUserByAdminDTOIfCurrentUserIsSuperAdmin() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        val currentUser = User(
                role = User.Role.SUPER_ADMIN
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                id = user.id,
                username = user.username
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(readUserByAdminDTOMapper.convert(user)).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.getByUsername(user.username, currentUser)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findByUsername(user.username)
        verify(readUserByAdminDTOMapper).convert(user)
    }

    @Test
    fun updateShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val updateUserDTO = UpdateUserDTO(
                email = "gleroy@test.com",
                password = "123456"
        )

        try {
            // WHEN
            userService.update(user.id, updateUserDTO)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User ${user.id} not found")
        }
    }

    @Test
    fun updateShouldThrowObjectAlreadyExistsExceptionIfTheEmailIsAlreadyUsed() {
        // GIVEN
        val user = User(
                id = UUID.randomUUID()
        )
        val updateUserDTO = UpdateUserDTO(
                email = "gleroy@test.com",
                password = "123456"
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.existsByEmail(updateUserDTO.email)).thenReturn(true)

        try {
            // WHEN
            userService.update(user.id, updateUserDTO)

            // THEN
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Email ${updateUserDTO.email} already used")
            verify(userRepository).findOne(user.id)
            verify(userRepository).existsByEmail(updateUserDTO.email)
        }
    }

    @Test
    fun updateShouldReturnReadUserByOwnerDTOIfByAdminIsFalse() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
                id = UUID.randomUUID(),
                password = passwordEncoder.encode("1234567"),
                role = User.Role.ADMIN,
                username = "gleroy"
        )
        val updateUserDTO = UpdateUserDTO(
                email = "gleroy2@test.com",
                password = "123456"
        )
        val readUserByOwnerDTO = ReadUserByOwnerDTO(
                email = updateUserDTO.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsUpdatedFromDTOAndReturnIt(user, invocation.getArgumentAt(0, User::class.java), updateUserDTO)
        })
        whenever(readUserByOwnerDTOMapper.convert(any<User>())).thenReturn(readUserByOwnerDTO)

        // WHEN
        val readUserDTO = userService.update(user.id, updateUserDTO)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByOwnerDTO)
        verify(userRepository).findOne(user.id)
        verify(userRepository).existsByEmail(updateUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByOwnerDTOMapper).convert(any<User>())
    }

    @Test
    fun updateShouldReturnReadUserByAdminDTOIfByAdminIsTrue() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
                id = UUID.randomUUID(),
                password = passwordEncoder.encode("1234567"),
                role = User.Role.ADMIN,
                username = "gleroy"
        )
        val updateUserDTO = UpdateUserDTO(
                email = "gleroy2@test.com",
                password = "123456"
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = updateUserDTO.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then({invocation ->
            assertUserIsUpdatedFromDTOAndReturnIt(user, invocation.getArgumentAt(0, User::class.java), updateUserDTO)
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.update(user.id, updateUserDTO, true)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(userRepository).existsByEmail(updateUserDTO.email)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    private fun assertUserIsCreatedFromDTOAndReturnIt(
            user: User, userDTO: CreateUserDTO, byAdmin: Boolean = false
    ): User {
        assertThat(user.email).isEqualTo(userDTO.email)
        assertThat(user.enabled).isEqualTo(byAdmin)
        assertThat(user.isAccountNonExpired).isTrue()
        assertThat(user.isAccountNonLocked).isTrue()
        assertThat(user.isCredentialsNonExpired).isTrue()
        assertThat(passwordEncoder.matches(userDTO.password, user.password)).isTrue()
        assertThat(user.role).isEqualTo(User.Role.USER)
        return user
    }

    private fun assertUserIsUpdatedFromDTOAndReturnIt(userToUpdate: User, user: User, userDTO: UpdateUserDTO): User {
        assertThat(user.email).isEqualTo(userDTO.email)
        assertThat(user.enabled).isEqualTo(userToUpdate.enabled)
        assertThat(user.isAccountNonExpired).isEqualTo(userToUpdate.isAccountNonExpired)
        assertThat(user.isAccountNonLocked).isEqualTo(userToUpdate.isAccountNonLocked)
        assertThat(user.isCredentialsNonExpired).isEqualTo(userToUpdate.isCredentialsNonExpired)
        assertThat(passwordEncoder.matches(userDTO.password, user.password)).isTrue()
        assertThat(user.role).isEqualTo(userToUpdate.role)
        return user
    }
}