package com.wearefrancis.auth.security

import com.wearefrancis.auth.exception.InvalidJwtException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
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
            filterChain!!.doFilter(request, response)
        } else {
            val jwt = authorizationValue.substring(BEARER.length + 1)
            try {
                val username = jwtUtils.getUsernameFromJwt(jwt)
                try {
                    if (SecurityContextHolder.getContext().authentication == null) {
                        val user = jwtUserService.loadUserByUsername(username)
                        if (!jwtUtils.isValidJwt(jwt, user)) {
                            response!!.sendError(HttpStatus.UNAUTHORIZED.value())
                            logger.info("Authentication of $username failed")
                        } else {
                            val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authentication
                            logger.info("User $username authenticated")
                            filterChain!!.doFilter(request, response)
                        }
                    } else {
                        filterChain!!.doFilter(request, response)
                    }
                } catch (exception: UsernameNotFoundException) {
                    response!!.sendError(HttpStatus.UNAUTHORIZED.value())
                    logger.info("Authentication of $username failed")
                }
            } catch (exception: InvalidJwtException) {
                response!!.sendError(HttpStatus.UNAUTHORIZED.value())
                logger.info("Invalid JWT: $jwt")
            }
        }
    }
}