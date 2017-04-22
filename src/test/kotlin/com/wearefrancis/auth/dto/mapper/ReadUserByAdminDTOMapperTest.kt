package com.wearefrancis.auth.dto.mapper

import com.wearefrancis.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ReadUserByAdminDTOMapperTest {
    private lateinit var readUserByAdminDTOMapper: ReadUserByAdminDTOMapper

    @Before
    fun setUp() {
        readUserByAdminDTOMapper = ReadUserByAdminDTOMapper()
    }

    @Test
    fun convertShouldConvertUserToReadUserByAdminDTO() {
        // GIVEN
        val user = User(
                email = "gleroy@test.com",
                enabled = true,
                locked = true,
                password = "123456",
                role = User.Role.ROLE_ADMIN,
                username = "gleroy"
        )

        // WHEN
        val readUserByAdminDTO = readUserByAdminDTOMapper.convert(user)

        // THEN
        assertThat(readUserByAdminDTO.email).isEqualTo(user.email)
        assertThat(readUserByAdminDTO.enabled).isEqualTo(user.enabled)
        assertThat(readUserByAdminDTO.id).isEqualTo(user.id)
        assertThat(readUserByAdminDTO.locked).isEqualTo(!user.isAccountNonLocked)
        assertThat(readUserByAdminDTO.role).isEqualTo(user.role)
        assertThat(readUserByAdminDTO.username).isEqualTo(user.username)
    }

}