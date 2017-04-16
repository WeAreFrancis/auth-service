package com.wearefrancis.auth.controller

import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RequestMapping("/users")
@RestController
class UserController(private val userService: UserService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid userDTO: WriteUserDTO): ReadUserDTO {
        return userService.create(userDTO)
    }
}