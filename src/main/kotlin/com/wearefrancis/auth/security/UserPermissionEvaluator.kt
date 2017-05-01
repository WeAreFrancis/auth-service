package com.wearefrancis.auth.security

import com.wearefrancis.auth.domain.User
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable

class UserPermissionEvaluator: PermissionEvaluator {
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
                UPDATE_PERMISSION -> currentUser!!.id == targetId
                        || currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                DELETE_PERMISSION -> currentUser!!.id == targetId
                        || currentUser.role == User.Role.SUPER_ADMIN
                else -> throw IllegalArgumentException("Invalid permission: $permission")
            }
            else -> throw IllegalArgumentException("Invalid target type: $targetType")
        }
    }
}