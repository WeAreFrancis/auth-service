package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.NAME_MAX_LENGTH
import org.hibernate.validator.constraints.Email
import javax.validation.constraints.Pattern

data class WriteUserDTO(
        @get:Email
        val email: String,

        @get:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}$")
        val password: String,

        @get:Pattern(regexp = "^[A-z0-9]{3,$NAME_MAX_LENGTH}$")
        val username: String
)