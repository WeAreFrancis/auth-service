package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ReadUserByOwnerDTOMapperTest {
    private lateinit var readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper

    @Before
    fun setUp() {
        readUserByOwnerDTOMapper = ReadUserByOwnerDTOMapper()
    }

    @Test
    fun convertShouldConvertUserToReadUserByOwnerDTO() {
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
        val readUserByOwnerDTO = readUserByOwnerDTOMapper.convert(user)

        // THEN
        assertThat(readUserByOwnerDTO.email).isEqualTo(user.email)
        assertThat(readUserByOwnerDTO.id).isEqualTo(user.id)
        assertThat(readUserByOwnerDTO.role).isEqualTo(user.role)
        assertThat(readUserByOwnerDTO.username).isEqualTo(user.username)
    }

}