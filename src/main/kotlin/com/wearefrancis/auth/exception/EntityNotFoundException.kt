package com.wearefrancis.auth.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class EntityNotFoundException(message: String) : RuntimeException(message) {
    companion object {
        val logger = LoggerFactory.getLogger(EntityNotFoundException::class.java)!!
    }

    init {
        logger.info(message)
    }
}