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
            WHERE user.email = :email
                    AND NOT user.id = :userId
    """)
    fun existsByEmailAndIdNot(@Param("email") email: String, @Param("userId") userId: UUID): Boolean

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.username = :username
    """)
    fun existsByUsername(@Param("username") username: String): Boolean

    fun findByUsername(username: String): User?
}