package com.wearefrancis.auth.security

import com.wearefrancis.auth.exception.InvalidJwtException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.*
import java.util.*

@Component
open class JwtUtils(
        val clock: Clock,
        @Value("\${jwt.expiration}") val expiration: Long,
        @Value("\${jwt.secret}") val secret: String
) {
    companion object {
        val logger = LoggerFactory.getLogger(JwtUtils::class.java)!!
    }

    open fun generateJwt(user: UserDetails): String = Jwts
            .builder()
            .setClaims(
                    mapOf(
                            CLAIM_KEY_USERNAME to user.username,
                            CLAIM_KEY_CREATED to LocalDateTime.from(clock.instant().atZone(clock.zone))
                    )
            )
            .setExpiration(Date.from(clock.instant().plusSeconds(expiration)))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    open fun getExpirationDateFromJwt(jwt: String): LocalDate {
        val claims = getClaimsFromJwt(jwt) ?: throw InvalidJwtException("$jwt is invalid")
        return LocalDate.from(claims.expiration.toInstant().atZone(clock.zone))
    }

    open fun getUsernameFromJwt(jwt: String): String {
        val claims = getClaimsFromJwt(jwt) ?: throw InvalidJwtException("$jwt is invalid")
        return claims.subject
    }

    open fun isValidJwt(jwt: String, user: UserDetails): Boolean {
        val claims = getClaimsFromJwt(jwt) ?: return false
        return user.isAccountNonExpired
                && user.isAccountNonLocked
                && user.isCredentialsNonExpired
                && user.isEnabled
                && user.username == claims.subject
    }

    private fun getClaimsFromJwt(jwt: String): Claims? {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(jwt)
                    .body
        } catch (exception: Exception) {
            logger.debug("$jwt is invalid", exception)
            return null
        }
    }
}