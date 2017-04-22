package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.User
import java.util.*

data class ReadUserByAdminDTO(
        val email: String,
        val enabled: Boolean = false,
        override val id: UUID = UUID.randomUUID(),
        val locked: Boolean = false,
        val role: User.Role = User.Role.ROLE_USER,
        override val username: String
) : ReadUserDTO