package com.wearefrancis.auth.domain

import com.wearefrancis.auth.NAME_MAX_LENGTH
import org.hibernate.annotations.GenericGenerator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
        @Column(nullable = false, unique = true)
        val email: String = "",

        @Column(nullable = false)
        val enabled: Boolean = false,

        @GeneratedValue(generator = "uuid2")
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        @Id
        val id: UUID = UUID.randomUUID(),

        @Column(nullable = false)
        private val locked: Boolean = false,

        @Column(nullable = false)
        private val password: String = "",

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        val role: Role = User.Role.USER,

        @Column(length = NAME_MAX_LENGTH, nullable = false, unique = true)
        private val username: String = ""
) : Serializable, UserDetails {
    enum class Role : GrantedAuthority {
        USER,
        ADMIN,
        SUPER_ADMIN;

        override fun getAuthority(): String {
            return name
        }
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = Collections.singletonList(role)

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = !locked

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled
}