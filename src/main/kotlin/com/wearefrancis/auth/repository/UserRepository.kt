package com.wearefrancis.auth.repository

import com.wearefrancis.auth.domain.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : CrudRepository<User, UUID> {
    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.email = :email
    """)
    fun existsByEmail(@Param("email") email: String): Boolean

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.username = :username
    """)
    fun existsByUsername(@Param("username") username: String): Boolean
}