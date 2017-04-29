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
        val currentUser = when (authentication) {
            null -> null
            else -> authentication.principal as User
        }
        return when (targetType) {
            "user" -> when (permission) {
                "create" -> currentUser == null
                        || currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN
                "update" -> currentUser != null
                        && (currentUser.id == targetId
                        || currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN)
                else -> throw IllegalArgumentException("Invalid permission: $permission")
            }
            else -> throw IllegalArgumentException("Invalid target type: $targetType")
        }
    }
}