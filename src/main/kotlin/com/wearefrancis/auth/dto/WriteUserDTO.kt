package com.wearefrancis.auth.dto

import org.hibernate.validator.constraints.Email
import javax.validation.constraints.Pattern

interface WriteUserDTO {
        @get:Email
        val email: String

        @get:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}$")
        val password: String
}