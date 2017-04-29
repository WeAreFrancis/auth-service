package com.wearefrancis.auth.controller

import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid

@RequestMapping("/users")
@RestController
class UserController(
        private val userService: UserService
) {
    @PostMapping
    @PreAuthorize("hasPermission(null, 'user', 'create')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid userDTO: WriteUserDTO, principal: Principal?): ReadUserDTO {
        return userService.create(userDTO, principal != null)
    }
}