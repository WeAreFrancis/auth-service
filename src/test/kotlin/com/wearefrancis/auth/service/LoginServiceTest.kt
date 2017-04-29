package com.wearefrancis.auth.service

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.exception.BadCredentialsException
import com.wearefrancis.auth.repository.UserRepository
import com.wearefrancis.auth.security.JwtUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class LoginServiceTest {
    private lateinit var loginService: LoginService

    @Mock
    private lateinit var jwtUtils: JwtUtils

    @Mock
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        loginService = LoginService(
                jwtUtils = jwtUtils,
                passwordEncoder = BCryptPasswordEncoder(),
                userRepository = userRepository
        )
    }

    @Test
    fun loginShouldThrowBadCredentialsExceptionIfUserThatHasTheGivenUsernameIsNotFound() {
        // GIVEN
        val username = "gleroy"

        try {
            // WHEN
            loginService.login(username, "123456")

            // THEN
            fail()
        } catch (exception: BadCredentialsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Bad credentials")
            verify(userRepository).findByUsername(username)
        }
    }

    @Test
    fun loginShouldThrowBadCredentialsExceptionIfPasswordsDoNotMatch() {
        // GIVEN
        val password = "123456"
        val user = User(
                password = loginService.passwordEncoder.encode(password),
                username = "gleroy"
        )
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)

        try {
            // WHEN
            loginService.login(user.username, "${password}7")

            // THEN
            fail()
        } catch (exception: BadCredentialsException) {
            // THEN
            assertThat(exception.message).isEqualTo("Bad credentials")
            verify(userRepository).findByUsername(user.username)
        }
    }

    @Test
    fun loginShouldReturnTheCreatedJwt() {
        // GIVEN
        val password = "123456"
        val user = User(
                password = loginService.passwordEncoder.encode(password),
                username = "gleroy"
        )
        val jwt = "jwt"
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(jwtUtils.generateJwt(user)).thenReturn(jwt)

        // WHEN
        val jwtDTO = loginService.login(user.username, password)

        // THEN
        assertThat(jwtDTO.jwt).isSameAs(jwt)
        verify(userRepository).findByUsername(user.username)
        verify(jwtUtils).generateJwt(user)
    }
}