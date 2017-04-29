package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ReadUserByUserDTOMapperTest {
    private lateinit var readUserByUserDTOMapper: ReadUserByUserDTOMapper

    @Before
    fun setUp() {
        readUserByUserDTOMapper = ReadUserByUserDTOMapper()
    }

    @Test
    fun convertShouldConvertUserToReadUserByUserDTO() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
                locked = true,
                password = "123456",
                role = User.Role.ADMIN,
                username = "gleroy"
        )

        // WHEN
        val readUserByUserDTO = readUserByUserDTOMapper.convert(user)

        // THEN
        assertThat(readUserByUserDTO.id).isEqualTo(user.id)
        assertThat(readUserByUserDTO.username).isEqualTo(user.username)
    }

}