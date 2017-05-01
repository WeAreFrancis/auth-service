package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.User

data class WriteUserRoleDTO(
        val role: User.Role
)