package com.wearefrancis.auth.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.Token
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.*
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByUserDTOMapper
import com.wearefrancis.auth.exception.EntityNotFoundException
import com.wearefrancis.auth.exception.ObjectAlreadyExistsException
import com.wearefrancis.auth.repository.TokenRepository
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
    private lateinit var tokenRepository: TokenRepository
    private lateinit var tokenService: TokenService
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @Before
    fun setUp() {
        readUserByAdminDTOMapper = mock<ReadUserByAdminDTOMapper>()
        readUserByOwnerDTOMapper = mock<ReadUserByOwnerDTOMapper>()
        readUserByUserDTOMapper = mock<ReadUserByUserDTOMapper>()
        tokenRepository = mock<TokenRepository>()
        tokenService = mock<TokenService>()
        userRepository = mock<UserRepository>()
        userService = UserService(
                passwordEncoder = passwordEncoder,
                readUserByAdminDTOMapper = readUserByAdminDTOMapper,
                readUserByOwnerDTOMapper = readUserByOwnerDTOMapper,
                readUserByUserDTOMapper = readUserByUserDTOMapper,
                tokenRepository = tokenRepository,
                tokenService = tokenService,
                userRepository = userRepository
        )
    }

    @Test
    fun activateShouldThrowEntityNotFoundExceptionIfTokenThatHasTheGivenValueIsNotFound() {
        // GIVEN
        val tokenValue = UUID.randomUUID()

        try {
            // WHEN
            userService.activate(tokenValue)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("Token $tokenValue not found")
            verify(tokenRepository).findByValue(tokenValue)
        }
    }
    
    @Test
    fun activateShouldEnableUserAndDeleteToken() {
        // GIVEN
        val user = User()
        val token = Token(
                user = user
        )
        whenever(tokenRepository.findByValue(token.value)).thenReturn(token)
        whenever(userRepository.save(any<User>())).then(fun (invocation): User {
            val savedUser = invocation.getArgumentAt(0, User::class.java)
            assertThat(savedUser.email).isEqualTo(user.email)
            assertThat(savedUser.enabled).isTrue()
            assertThat(savedUser.isAccountNonExpired).isEqualTo(user.isAccountNonExpired)
            assertThat(savedUser.isAccountNonLocked).isEqualTo(user.isAccountNonLocked)
            assertThat(savedUser.isCredentialsNonExpired).isEqualTo(user.isCredentialsNonExpired)
            assertThat(savedUser.password).isEqualTo(user.password)
            assertThat(savedUser.role).isEqualTo(user.role)
            return user
        })
        
        // WHEN
        userService.activate(token.value)
        
        // THEN
        verify(tokenRepository).findByValue(token.value)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun changeRoleShouldThrowEntityNotFoundExceptionIfUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val userId = UUID.randomUUID()
        val userRoleDTO = WriteUserRoleDTO(
                role = User.Role.USER
        )

        try {
            // WHEN
            userService.changeRole(userId, userRoleDTO)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $userId not found")
            verify(userRepository).findOne(userId)
        }
    }

    @Test
    fun changeRoleShouldChangeUserRole() {
        // GIVEN
        val user = User()
        val userRoleDTO = WriteUserRoleDTO(
                role = User.Role.SUPER_ADMIN
        )
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then(fun (invocation): User {
            val model = invocation.getArgumentAt(0, User::class.java)
            assertThat(model.email).isEqualTo(user.email)
            assertThat(model.enabled).isEqualTo(user.enabled)
            assertThat(model.isAccountNonExpired).isEqualTo(user.isAccountNonExpired)
            assertThat(model.isAccountNonLocked).isEqualTo(user.isAccountNonLocked)
            assertThat(model.isCredentialsNonExpired).isEqualTo(user.isCredentialsNonExpired)
            assertThat(model.password).isEqualTo(user.password)
            assertThat(model.role).isEqualTo(userRoleDTO.role)
            return model
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.changeRole(user.id, userRoleDTO)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    @Test
    fun createShouldThrowObjectAlreadyExistsExceptionIfTheUsernameIsAlreadyUsed() {
        // GIVEN
        val createUserDTO = CreateUserDTO(
                email = "gleroy@test.com",
                password = "123456",
                username = "gleroy"
        )
        whenever(userRepository.existsByUsername(createUserDTO.username)).thenReturn(true)

        try {
            // WHEN
            userService.create(createUserDTO)

            // THEN
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Username ${createUserDTO.username} already used")
            verify(userRepository).existsByUsername(createUserDTO.username)
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
        whenever(userRepository.existsByEmail(createUserDTO.email)).thenReturn(true)

        try {
            // WHEN
            userService.create(createUserDTO)

            // THEN
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Email ${createUserDTO.email} already used")
            verify(userRepository).existsByUsername(createUserDTO.username)
            verify(userRepository).existsByEmail(createUserDTO.email)
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
        verify(tokenService).sendMail(any<User>())
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
    fun deleteShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val userId = UUID.randomUUID()

        try {
            // WHEN
            userService.delete(userId)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $userId not found")
            verify(userRepository).exists(userId)
        }
    }

    @Test
    fun deleteShouldDeleteUser() {
        // GIVEN
        val userId = UUID.randomUUID()
        whenever(userRepository.exists(userId)).thenReturn(true)

        // WHEN
        userService.delete(userId)

        // THEN
        verify(userRepository).exists(userId)
        verify(userRepository).delete(userId)
    }

    @Test
    fun enableShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val userId = UUID.randomUUID()

        try {
            // WHEN
            userService.enable(userId)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $userId not found")
            verify(userRepository).findOne(userId)
        }
    }

    @Test
    fun enableShouldEnableUser() {
        // GIVEN
        val user = User()
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then(fun (invocation): User {
            val model = invocation.getArgumentAt(0, User::class.java)
            assertThat(model.email).isEqualTo(user.email)
            assertThat(model.enabled).isTrue()
            assertThat(model.isAccountNonExpired).isEqualTo(user.isAccountNonExpired)
            assertThat(model.isAccountNonLocked).isEqualTo(user.isAccountNonLocked)
            assertThat(model.isCredentialsNonExpired).isEqualTo(user.isCredentialsNonExpired)
            assertThat(model.password).isEqualTo(user.password)
            assertThat(model.role).isEqualTo(user.role)
            return model
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.enable(user.id)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
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
        val user = User()
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
        val user = User()
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
        val user = User()
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
        val user = User()
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
    fun lockShouldThrowEntityNotFoundExceptionIfUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val userId = UUID.randomUUID()

        try {
            // WHEN
            userService.lock(userId)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $userId not found")
            verify(userRepository).findOne(userId)
        }
    }

    @Test
    fun lockShouldLockUser() {
        // GIVEN
        val user = User()
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then(fun (invocation): User {
            val model = invocation.getArgumentAt(0, User::class.java)
            assertThat(model.email).isEqualTo(user.email)
            assertThat(model.enabled).isEqualTo(user.enabled)
            assertThat(model.isAccountNonExpired).isEqualTo(user.isAccountNonExpired)
            assertThat(model.isAccountNonLocked).isFalse()
            assertThat(model.isCredentialsNonExpired).isEqualTo(user.isCredentialsNonExpired)
            assertThat(model.password).isEqualTo(user.password)
            assertThat(model.role).isEqualTo(user.role)
            return model
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.lock(user.id)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    @Test
    fun unlockShouldThrowEntityNotFoundExceptionIfUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val userId = UUID.randomUUID()

        try {
            // WHEN
            userService.unlock(userId)

            // THEN
            fail()
        } catch (exception: EntityNotFoundException) {
            // THEN
            assertThat(exception.message).isEqualTo("User $userId not found")
            verify(userRepository).findOne(userId)
        }
    }

    @Test
    fun unlockShouldUnlockUser() {
        // GIVEN
        val user = User()
        val readUserByAdminDTO = ReadUserByAdminDTO(
                email = user.email,
                username = user.username
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.save(any<User>())).then(fun (invocation): User {
            val model = invocation.getArgumentAt(0, User::class.java)
            assertThat(model.email).isEqualTo(user.email)
            assertThat(model.enabled).isEqualTo(user.enabled)
            assertThat(model.isAccountNonExpired).isEqualTo(user.isAccountNonExpired)
            assertThat(model.isAccountNonLocked).isTrue()
            assertThat(model.isCredentialsNonExpired).isEqualTo(user.isCredentialsNonExpired)
            assertThat(model.password).isEqualTo(user.password)
            assertThat(model.role).isEqualTo(user.role)
            return model
        })
        whenever(readUserByAdminDTOMapper.convert(any<User>())).thenReturn(readUserByAdminDTO)

        // WHEN
        val readUserDTO = userService.unlock(user.id)

        // THEN
        assertThat(readUserDTO).isSameAs(readUserByAdminDTO)
        verify(userRepository).findOne(user.id)
        verify(userRepository).save(any<User>())
        verify(readUserByAdminDTOMapper).convert(any<User>())
    }

    @Test
    fun updateShouldThrowEntityNotFoundExceptionIfTheUserThatHasTheGivenIdIsNotFound() {
        // GIVEN
        val user = User()
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
        val user = User()
        val updateUserDTO = UpdateUserDTO(
                email = "gleroy@test.com",
                password = "123456"
        )
        whenever(userRepository.findOne(user.id)).thenReturn(user)
        whenever(userRepository.existsByEmailAndIdNot(updateUserDTO.email, user.id)).thenReturn(true)

        try {
            // WHEN
            userService.update(user.id, updateUserDTO)

            // THEN
            fail()
        } catch (exception: ObjectAlreadyExistsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Email ${updateUserDTO.email} already used")
            verify(userRepository).findOne(user.id)
            verify(userRepository).existsByEmailAndIdNot(updateUserDTO.email, user.id)
        }
    }

    @Test
    fun updateShouldReturnReadUserByOwnerDTOIfByAdminIsFalse() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
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
        verify(userRepository).existsByEmailAndIdNot(updateUserDTO.email, user.id)
        verify(userRepository).save(any<User>())
        verify(readUserByOwnerDTOMapper).convert(any<User>())
    }

    @Test
    fun updateShouldReturnReadUserByAdminDTOIfByAdminIsTrue() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
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
        verify(userRepository).existsByEmailAndIdNot(updateUserDTO.email, user.id)
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