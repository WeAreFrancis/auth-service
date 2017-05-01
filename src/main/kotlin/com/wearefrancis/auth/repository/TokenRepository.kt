package com.wearefrancis.auth.repository

import com.wearefrancis.auth.domain.Token
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TokenRepository: CrudRepository<Token, UUID>