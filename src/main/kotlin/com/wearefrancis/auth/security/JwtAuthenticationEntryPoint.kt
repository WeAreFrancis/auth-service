package com.wearefrancis.auth.security

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            authenticationException: AuthenticationException?
    ) = response!!.sendError(HttpStatus.UNAUTHORIZED.value())
}