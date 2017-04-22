package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserByOwnerDTO
import org.springframework.stereotype.Component

@Component
open class ReadUserByOwnerDTOMapper : Mapper<User, ReadUserByOwnerDTO> {
    override fun convert(model: User): ReadUserByOwnerDTO = ReadUserByOwnerDTO(
            email = model.email,
            id = model.id,
            role = model.role,
            username = model.username
    )
}