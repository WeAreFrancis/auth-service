package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserByUserDTO
import org.springframework.stereotype.Component

@Component
open class ReadUserByUserDTOMapper : Mapper<User, ReadUserByUserDTO> {
    override fun convert(model: User): ReadUserByUserDTO = ReadUserByUserDTO(
            id = model.id,
            username = model.username
    )
}