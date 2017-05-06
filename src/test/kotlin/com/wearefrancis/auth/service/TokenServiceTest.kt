package com.wearefrancis.auth.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.wearefrancis.auth.domain.Token
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.repository.TokenRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import org.thymeleaf.TemplateEngine

class TokenServiceTest {
    private val address: String = "test@test.com"
    private val apiUrl = "http://localhost"
    private lateinit var mailSender: JavaMailSender
    private val subject = "subject"
    private lateinit var templateEngine: TemplateEngine
    private lateinit var tokenRepository: TokenRepository
    private lateinit var tokenService: TokenService

    @Before
    fun setUp() {
        mailSender = mock<JavaMailSender>()
        templateEngine = mock<TemplateEngine>()
        tokenRepository = mock<TokenRepository>()
        tokenService = TokenService(
                address = address,
                apiUrl = apiUrl,
                mailSender = mailSender,
                subject = subject,
                templateEngine = templateEngine,
                tokenRepository = tokenRepository
        )
    }

    @Test
    fun sendMailShouldCreateTokenAndSendMail() {
        // GIVEN
        val user = User()
        whenever(tokenRepository.save(any<Token>())).then(fun (invocation): Token {
            val token = invocation.getArgumentAt(0, Token::class.java)
            assertThat(token.user).isSameAs(user)
            return token
        })

        // WHEN
        tokenService.sendMail(user)

        // THEN
        verify(tokenRepository).save(any<Token>())
    }

}