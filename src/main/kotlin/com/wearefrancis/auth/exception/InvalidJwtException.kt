package com.wearefrancis.auth.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidJwtException(message: String) : RuntimeException(message) {
    companion object {
        val logger = LoggerFactory.getLogger(InvalidJwtException::class.java)!!
    }

    init {
        logger.error(message)
    }
}