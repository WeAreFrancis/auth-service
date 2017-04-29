package com.wearefrancis.auth.controller

import com.wearefrancis.auth.dto.JwtDTO
import com.wearefrancis.auth.service.LoginService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull

@RequestMapping("/login")
@RestController
class LoginController(
        val loginService: LoginService
) {
    @GetMapping
    fun login(@NotNull username: String?, @NotNull password: String?): JwtDTO
            = loginService.login(username!!, password!!)
}