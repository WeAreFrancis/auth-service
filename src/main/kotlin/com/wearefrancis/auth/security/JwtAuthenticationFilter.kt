package com.wearefrancis.auth.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class JwtAuthenticationFilter(
        val jwtUserService: JwtUserService,
        val jwtUtils: JwtUtils
) : OncePerRequestFilter() {
    override fun doFilterInternal(
            request: HttpServletRequest?, response: HttpServletResponse?, filterChain: FilterChain?
    ) {
        val authorizationValue = request!!.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorizationValue == null || !authorizationValue.startsWith(BEARER, true)) {
            logger.info("Invalid AUTHORIZATION header value")
        } else {
            val jwt = authorizationValue.substring(BEARER.length + 1)
            val username = jwtUtils.getUsernameFromJwt(jwt)
            if (SecurityContextHolder.getContext().authentication == null) {
                val user = jwtUserService.loadUserByUsername(username)
                if (!jwtUtils.isValidJwt(jwt, user)) {
                    logger.info("Authentication of $username failed")
                } else {
                    val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.info("User $username authenticated")
                }
            }
        }
        filterChain!!.doFilter(request, response)
    }
}