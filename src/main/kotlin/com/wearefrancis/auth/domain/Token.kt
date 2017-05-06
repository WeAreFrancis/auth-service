package com.wearefrancis.auth.domain

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tokens")
data class Token(
        @OneToOne(optional = false)
        val user: User = User(),

        @GeneratedValue(generator = "uuid2")
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        @Id
        val value: UUID = UUID.randomUUID()
)