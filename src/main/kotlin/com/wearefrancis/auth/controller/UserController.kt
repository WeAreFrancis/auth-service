package com.wearefrancis.auth.controller

import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.WriteUserDTO
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid

@RequestMapping("/users")
@RestController
class UserController(
        private val readUserByAdminDTOMapper: ReadUserByAdminDTOMapper,
        private val readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper,
        private val userService: UserService
) {
    @PostMapping
    @PreAuthorize("hasPermission(null, 'user', 'create')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid userDTO: WriteUserDTO, principal: Principal?): ReadUserDTO {
        return userService.create(userDTO, principal != null)
    }

    @GetMapping
    fun getCurrentUser(principal: Principal): ReadUserDTO {
        val token = principal as UsernamePasswordAuthenticationToken
        val user = token.principal as User
        return when {
            user.role in User.Role.ADMIN..User.Role.SUPER_ADMIN -> readUserByAdminDTOMapper.convert(user)
            else -> readUserByOwnerDTOMapper.convert(user)
        }
    }
}