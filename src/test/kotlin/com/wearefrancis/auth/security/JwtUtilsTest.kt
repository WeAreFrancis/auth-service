package com.wearefrancis.auth.security

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.*

class JwtUtilsTest {
    @Mock
    private lateinit var clock: Clock

    private val expiration: Long = 86400

    private lateinit var jwtUtils: JwtUtils

    private val secret: String = "secret"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        jwtUtils = JwtUtils(
                clock = clock,
                expiration = expiration,
                secret = secret
        )
    }

    @Test
    fun generateJwtShouldGenerateJwt() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        val now = Instant.now()
        val zone = ZoneOffset.UTC
        whenever(clock.instant()).thenReturn(now)
        whenever(clock.zone).thenReturn(zone)

        // WHEN
        val jwt = jwtUtils.generateJwt(user)

        // THEN
        assertThat(jwtUtils.getUsernameFromJwt(jwt)).isEqualTo(user.username)
        assertThat(jwtUtils.getExpirationDateFromJwt(jwt))
                .isEqualTo(LocalDate.from(now.plusSeconds(expiration).atZone(zone)))
        verify(clock, times(2)).instant()
        verify(clock, times(2)).zone
    }

    @Test
    fun isValidJwtShouldReturnFalseIfUserIsLocked() {
        // GIVEN
        val user = User(
                enabled = true,
                locked = true,
                username = "gleroy"
        )
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)
        val jwt = jwtUtils.generateJwt(user)

        // WHEN
        val valid = jwtUtils.isValidJwt(jwt, user)

        // THEN
        assertThat(valid).isFalse()
        verify(clock, times(2)).instant()
        verify(clock).zone
    }

    @Test
    fun isValidJwtShouldReturnFalseIfUserIsNotEnabled() {
        // GIVEN
        val user = User(
                username = "gleroy"
        )
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)
        val jwt = jwtUtils.generateJwt(user)

        // WHEN
        val valid = jwtUtils.isValidJwt(jwt, user)

        // THEN
        assertThat(valid).isFalse()
        verify(clock, times(2)).instant()
        verify(clock).zone
    }

    @Test
    fun isValidJwtShouldReturnFalseIfUsernameIsNotEqualToSubject() {
        // GIVEN
        val user = User(
                enabled = true,
                username = "gleroy"
        )
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)
        val jwt = jwtUtils.generateJwt(User(
                enabled = true,
                username = "${user.username}2"
        ))

        // WHEN
        val valid = jwtUtils.isValidJwt(jwt, user)

        // THEN
        assertThat(valid).isFalse()
        verify(clock, times(2)).instant()
        verify(clock).zone
    }

    @Test
    fun isValidJwtShouldReturnFalseIfJwtIsExpired() {
        // GIVEN
        val user = User(
                enabled = true,
                username = "gleroy"
        )
        whenever(clock.instant()).thenReturn(Instant.ofEpochMilli(1))
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)
        val jwt = jwtUtils.generateJwt(user)
        whenever(clock.instant()).thenReturn(Instant.now())

        // WHEN
        val valid = jwtUtils.isValidJwt(jwt, user)

        // THEN
        assertThat(valid).isFalse()
        verify(clock, times(2)).instant()
        verify(clock).zone
    }

    @Test
    fun isValidJwtShouldReturnTrueIfJwtIsValid() {
        // GIVEN
        val user = User(
                enabled = true,
                username = "gleroy"
        )
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)
        val jwt = jwtUtils.generateJwt(user)

        // WHEN
        val valid = jwtUtils.isValidJwt(jwt, user)

        // THEN
        assertThat(valid).isTrue()
        verify(clock, times(2)).instant()
        verify(clock).zone
    }
}