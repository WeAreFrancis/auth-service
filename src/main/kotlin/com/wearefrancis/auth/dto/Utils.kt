package com.wearefrancis.auth.dto

import com.wearefrancis.auth.domain.User

fun userModelToReadDTO(model: User, currentUser: User): ReadUserDTO = when {
    currentUser.role in User.Role.ROLE_ADMIN..User.Role.ROLE_SUPER_ADMIN -> userModelToReadUserByAdminDTO(model)
    currentUser.id == model.id -> userModelToReadUserByOwnerDTO(model)
    else -> userModelToReadUserByUserDTO(model)
}

fun userModelToReadUserByAdminDTO(model: User): ReadUserByAdminDTO = ReadUserByAdminDTO(
        email = model.email,
        enabled = model.enabled,
        id = model.id,
        locked = !model.isAccountNonLocked,
        role = model.role,
        username = model.username
)

fun userModelToReadUserByOwnerDTO(model: User): ReadUserByOwnerDTO = ReadUserByOwnerDTO(
        email = model.email,
        id = model.id,
        role = model.role,
        username = model.username
)

fun userModelToReadUserByUserDTO(model: User): ReadUserByUserDTO = ReadUserByUserDTO(
        id = model.id,
        username = model.username
)