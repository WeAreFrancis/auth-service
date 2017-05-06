package com.wearefrancis.auth.dto

import com.wearefrancis.auth.USERNAME_REGEX
import javax.validation.constraints.Pattern

data class CreateUserDTO(
        override val email: String,

        override val password: String,

        @get:Pattern(regexp = USERNAME_REGEX)
        val username: String
) : WriteUserDTO