package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserByAdminDTO
import org.springframework.stereotype.Component

@Component
open class ReadUserByAdminDTOMapper : Mapper<User, ReadUserByAdminDTO> {
    override fun convert(model: User): ReadUserByAdminDTO = ReadUserByAdminDTO(
            email = model.email,
            enabled = model.enabled,
            id = model.id,
            locked = !model.isAccountNonLocked,
            role = model.role,
            username = model.username
    )
}