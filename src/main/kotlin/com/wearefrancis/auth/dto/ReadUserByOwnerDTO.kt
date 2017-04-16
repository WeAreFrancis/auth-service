package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.User
import java.util.*

data class ReadUserByOwnerDTO(
        val email: String,
        override val id: UUID,
        val role: User.Role,
        override val username: String
) : ReadUserDTO