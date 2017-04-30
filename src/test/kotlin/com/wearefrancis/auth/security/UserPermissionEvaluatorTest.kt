package com.wearefrancis.auth.security

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.Authentication
import java.util.*

class UserPermissionEvaluatorTest {
    private lateinit var userPermissionEvaluator: UserPermissionEvaluator

    @Before
    fun setUp() {
        userPermissionEvaluator = UserPermissionEvaluator()
    }

    @Test
    fun hasPermissionShouldThrowIllegalArgumentExceptionIfTargetTypeIsInvalid() {
        // GIVEN
        val targetType = "user2"

        try {
            // WHEN
            userPermissionEvaluator.hasPermission(null, null, targetType, "create")

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
        val permission = "getById"

        try {
            // WHEN
            userPermissionEvaluator.hasPermission(null, null, "user", permission)

            // THEN
            fail()
        } catch (exception: IllegalArgumentException) {
            // THEN
            assertThat(exception.message).isEqualTo("Invalid permission: $permission")
        }
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsCreateAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, null, "user", "create")

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsCreateAndCurrentUserIsAnonymous() {
        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(null, null, "user", "create")

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
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, null, "user", "create")

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsCreateAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, null, "user", "create")

        assertThat(hasPermission).isTrue()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnFalseIfPermissionIsUpdateAndCurrentUserIsUser() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User())

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, UUID.randomUUID(), "user", "update")

        // THEN
        assertThat(hasPermission).isFalse()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsOwner() {
        // GIVEN
        val authentication = mock<Authentication>()
        val user = User()
        whenever(authentication.principal).thenReturn(user)

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, user.id, "user", "update")

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, UUID.randomUUID(), "user", "update")

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication).principal
    }

    @Test
    fun hasPermissionShouldReturnTrueIfPermissionIsUpdateAndCurrentUserIsSuperAdmin() {
        // GIVEN
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(User(
                role = User.Role.SUPER_ADMIN
        ))

        // WHEN
        val hasPermission = userPermissionEvaluator.hasPermission(authentication, UUID.randomUUID(), "user", "update")

        // THEN
        assertThat(hasPermission).isTrue()
        verify(authentication).principal
    }
}