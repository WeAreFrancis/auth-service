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
    ): Boolean = when (targetType) {
        "user" -> when(permission) {
            "create" -> authentication == null
                    || User.Role.ADMIN in authentication.authorities
                    || User.Role.SUPER_ADMIN in authentication.authorities
            else -> false
        }
        else -> false
    }
}