package com.wearefrancis.auth.security

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.security.core.userdetails.UsernameNotFoundException

class JwtUserServiceTest {
    private lateinit var jwtUserService: JwtUserService

    @Mock
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        jwtUserService = JwtUserService(userRepository)
    }

    @Test
    fun loadUserByUsernameShouldThrowUsernameNotFoundExceptionIfUserIsNotFound() {
        // GIVEN
        val username = "gleroy"

        try {
            // WHEN
            jwtUserService.loadUserByUsername(username)

            // THEN
            verify(userRepository).findByUsername(username)
            fail()
        } catch (exception: UsernameNotFoundException) {
            assertThat(exception.message).isEqualTo("User $username not found")
        }
    }

    @Test
    fun loadUserByUsernameShouldReturnTheUserThatHasTheGivenUsername() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)

        // WHEN
        val jwtUser = jwtUserService.loadUserByUsername(user.username)

        // THEN
        assertThat(jwtUser).isSameAs(user)
        verify(userRepository).findByUsername(user.username)
    }

}