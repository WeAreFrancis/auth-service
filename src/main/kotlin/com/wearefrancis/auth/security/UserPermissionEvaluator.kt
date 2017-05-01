package com.wearefrancis.auth.security

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.repository.UserRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable
import java.util.*

class UserPermissionEvaluator(
        private val userRepository: UserRepository
): PermissionEvaluator {
    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun hasPermission(
            authentication: Authentication?, targetId: Serializable?, targetType: String?, permission: Any?
    ): Boolean {
        val currentUser = when (authentication?.principal) {
            is User -> authentication.principal as User
            else -> null
        }
        return when (targetType) {
            USER_TARGET_TYPE -> when (permission) {
                CREATE_PERMISSION -> currentUser == null
                        || currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                DELETE_PERMISSION -> currentUser!!.id == targetId
                        || currentUser.role == User.Role.SUPER_ADMIN
                ENABLE_PERMISSION -> currentUser!!.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                LOCK_PERMISSION -> currentUser!!.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                            && currentUser.id != targetId
                            && !userRepository.existsByIdAndRole(targetId as UUID, User.Role.SUPER_ADMIN)
                UPDATE_PERMISSION -> currentUser!!.id == targetId
                        || currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                else -> throw IllegalArgumentException("Invalid permission: $permission")
            }
            else -> throw IllegalArgumentException("Invalid target type: $targetType")
        }
    }
}