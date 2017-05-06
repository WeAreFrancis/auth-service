package com.wearefrancis.auth.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ObjectAlreadyExistsException(message: String) : RuntimeException(message) {
    companion object {
        val logger = LoggerFactory.getLogger(ObjectAlreadyExistsException::class.java)!!
    }

    init {
        logger.info(message)
    }
}