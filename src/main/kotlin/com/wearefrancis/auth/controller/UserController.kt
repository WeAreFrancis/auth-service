package com.wearefrancis.auth.controller

import com.wearefrancis.auth.USERNAME_REGEX
import com.wearefrancis.auth.UUID_REGEX
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.dto.CreateUserDTO
import com.wearefrancis.auth.dto.ReadUserByAdminDTO
import com.wearefrancis.auth.dto.ReadUserDTO
import com.wearefrancis.auth.dto.UpdateUserDTO
import com.wearefrancis.auth.dto.mapper.ReadUserByAdminDTOMapper
import com.wearefrancis.auth.dto.mapper.ReadUserByOwnerDTOMapper
import com.wearefrancis.auth.security.*
import com.wearefrancis.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*
import javax.validation.Valid

@RequestMapping("/users")
@RestController
class UserController(
        private val readUserByAdminDTOMapper: ReadUserByAdminDTOMapper,
        private val readUserByOwnerDTOMapper: ReadUserByOwnerDTOMapper,
        private val userService: UserService
) {
    @PostMapping
    @PreAuthorize("hasPermission(null, '$USER_TARGET_TYPE', '$CREATE_PERMISSION')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid userDTO: CreateUserDTO, principal: Principal?): ReadUserDTO {
        return userService.create(userDTO, principal != null)
    }

    @DeleteMapping("/{userId:$UUID_REGEX}")
    @PreAuthorize("hasPermission(#userId, '$USER_TARGET_TYPE', '$DELETE_PERMISSION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable userId: UUID) {
        userService.delete(userId)
    }

    @PreAuthorize("hasPermission(#userId, '$USER_TARGET_TYPE', '$ENABLE_PERMISSION')")
    @PutMapping("/{userId:$UUID_REGEX}/enable")
    fun enable(@PathVariable userId: UUID): ReadUserByAdminDTO {
        return userService.enable(userId)
    }

    @GetMapping("/{userId:$UUID_REGEX}")
    fun getById(@PathVariable userId: UUID, principal: Principal): ReadUserDTO {
        val currentUser = userFromPrincipal(principal)
        return userService.getById(userId, currentUser)
    }

    @GetMapping("/{username:$USERNAME_REGEX}")
    fun getByUsername(@PathVariable username: String, principal: Principal): ReadUserDTO {
        val currentUser = userFromPrincipal(principal)
        return userService.getByUsername(username, currentUser)
    }

    @GetMapping
    fun getCurrentUser(principal: Principal): ReadUserDTO {
        val user = userFromPrincipal(principal)
        return when {
            user.role in User.Role.ADMIN..User.Role.SUPER_ADMIN -> readUserByAdminDTOMapper.convert(user)
            else -> readUserByOwnerDTOMapper.convert(user)
        }
    }

    @PreAuthorize("hasPermission(#userId, '$USER_TARGET_TYPE', '$LOCK_PERMISSION')")
    @PutMapping("/{userId:$UUID_REGEX}/lock")
    fun lock(@PathVariable userId: UUID): ReadUserByAdminDTO = userService.lock(userId)

    @PreAuthorize("hasPermission(#userId, '$USER_TARGET_TYPE', '$LOCK_PERMISSION')")
    @PutMapping("/{userId:$UUID_REGEX}/unlock")
    fun unlock(@PathVariable userId: UUID): ReadUserByAdminDTO = userService.unlock(userId)

    @PreAuthorize("hasPermission(#userId, '$USER_TARGET_TYPE', '$UPDATE_PERMISSION')")
    @PutMapping("/{userId:$UUID_REGEX}")
    fun update(
            @PathVariable userId: UUID, @RequestBody @Valid userDTO: UpdateUserDTO, principal: Principal
    ): ReadUserDTO {
        val currentUser = userFromPrincipal(principal)
        return userService.update(userId, userDTO, currentUser.role in User.Role.ADMIN..User.Role.SUPER_ADMIN)
    }

    private fun userFromPrincipal(principal: Principal): User {
        val token = principal as UsernamePasswordAuthenticationToken
        return token.principal as User
    }
}