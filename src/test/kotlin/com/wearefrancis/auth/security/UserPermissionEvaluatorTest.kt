package com.wearefrancis.auth.security

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.Authentication
import java.util.*

class UserPermissionEvaluatorTest {
    private lateinit var userPermissionEvaluator: UserPermissionEvaluator
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        userRepository = mock<UserRepository>()
        userPermissionEvaluator = UserPermissionEvaluator(userRepository)
    }

    @Test
    fun hasPermissionShouldThrowIllegalArgumentExceptionIfTargetTypeIsInvalid() {
        // GIVEN
        val targetType = "${USER_TARGET_TYPE}2"

        try {
            // WHEN
            userPermissionEvaluator.hasPermission(null, null, targetType, CREATE_PERMISSION)

            // THEN
            fail()
        } catch (exception: IllegalArgumentException) {
            // THEN
            assertThat(exception.message).isEqualTo("Invalid target type: $targetType")
        }
    }

    @Test
    fun hasPermissionShouldThrowIllegalArgumentExceptionIfPermissionIsInvalid() {
        // GIVEN
        val permission = "${CREATE_PERMISSION}2"

        try {
            // WHEN
            userPermissionEvaluator.hasPermission(null, null, USER_TARGET_TYPE, permission)

            // THEN
            fail()
        } catch (exception: IllegalArgumentException) {
            // THEN
            assertThat(exception.message).isEqualTo("Invalid permission: $permission")
        }
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsChangeRoleAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, CHANGE_ROLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsChangeRoleAndCurrentUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User()
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, CHANGE_ROLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsChangeRoleAndCurrentUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, CHANGE_ROLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsChangeRoleAndCurrentUserIsSuperAdminButOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User(
                role = User.Role.SUPER_ADMIN
        )
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, CHANGE_ROLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsChangeRoleAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, CHANGE_ROLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsCreateAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, null, USER_TARGET_TYPE, CREATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsCreateAndCurrentUserIsAnonymous() {
        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(null, null, USER_TARGET_TYPE, CREATE_PERMISSION)

        // THEN
        assertThat(hasPermission).isTrue()
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsCreateAndCurrentUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, null, USER_TARGET_TYPE, CREATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsCreateAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, null, USER_TARGET_TYPE, CREATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsDeleteAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, DELETE_PERMISSION
        )

        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsDeleteAndCurrentUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, DELETE_PERMISSION
        )

        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsDeleteAndCurrentUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User(
                id = UUID.randomUUID()
        )
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, DELETE_PERMISSION
        )

        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsDeleteAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, DELETE_PERMISSION
        )

        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsEnableAndUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, ENABLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsEnableAndUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User()
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, ENABLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsEnableAndUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, ENABLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsEnableAndUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, ENABLE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User()
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsAdminButOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User(
                role = User.Role.ADMIN
        )
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsAdminButUserToLockIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        val targetId = UUID.randomUUID()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))
        whenever(userRepository.existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)).thenReturn(true)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, targetId, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
        verify(userRepository).existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsSuperAdminButOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User(
                role = User.Role.SUPER_ADMIN
        )
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsLockAndUserIsSuperAdminButUserToLockIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        val targetId = UUID.randomUUID()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))
        whenever(userRepository.existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)).thenReturn(true)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, targetId, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
        verify(userRepository).existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsLockAndUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        val targetId = UUID.randomUUID()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, targetId, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
        verify(userRepository).existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsLockAndUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        val targetId = UUID.randomUUID()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, targetId, USER_TARGET_TYPE, LOCK_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
        verify(userRepository).existsByIdAndRole(targetId, User.Role.SUPER_ADMIN)
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsUpdateAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, UPDATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User()
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, user.id, USER_TARGET_TYPE, UPDATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, UPDATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(
                authentication, UUID.randomUUID(), USER_TARGET_TYPE, UPDATE_PERMISSION
        )

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication, times(2)).principal
    }
}