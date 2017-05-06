package com.wearefrancis.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
open class AuthApplication {
    @Bean
    open fun clock(): Clock = Clock.systemDefaultZone()!!
}

fun main(args: Array<String>) {
    SpringApplication.run(AuthApplication::class.java, *args)
}