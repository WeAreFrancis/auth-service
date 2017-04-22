package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.User
import java.util.*

data class ReadUserByOwnerDTO(
        val email: String,
        override val id: UUID = UUID.randomUUID(),
        val role: User.Role = User.Role.ROLE_USER,
        override val username: String
) : ReadUserDTO