package com.wearefrancis.auth.dto

import com.wearefrancis.auth.NAME_MAX_LENGTH
import javax.validation.constraints.Pattern

data class CreateUserDTO(
        override val email: String,

        override val password: String,

        @get:Pattern(regexp = "^[A-z0-9]{3,$NAME_MAX_LENGTH}$")
        val username: String
) : WriteUserDTO