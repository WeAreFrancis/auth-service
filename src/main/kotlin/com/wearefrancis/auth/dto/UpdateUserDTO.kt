package com.wearefrancis.auth.dto

data class UpdateUserDTO(
        override val email: String,
        override val password: String
) : WriteUserDTO