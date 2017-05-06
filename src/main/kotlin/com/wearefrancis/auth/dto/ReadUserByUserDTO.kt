package com.wearefrancis.auth.dto

import java.util.*

data class ReadUserByUserDTO(
        override val id: UUID,
        override val username: String
) : ReadUserDTO