package com.wearefrancis.auth.service

import com.wearefrancis.auth.domain.Token
import com.wearefrancis.auth.domain.User
import com.wearefrancis.auth.repository.TokenRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
open class TokenService(
        @Value("\${mail.address}") private val address: String,
        @Value("\${mail.apiUrl}") private val apiUrl: String,
        private val mailSender: JavaMailSender,
        private val tokenRepository: TokenRepository,
        @Value("\${mail.subject}") private val subject: String,
        private val templateEngine: TemplateEngine
) {
    companion object {
        val logger = LoggerFactory.getLogger(TokenService::class.java)
    }

    open fun sendMail(user: User) {
        val token = Token(
                user = user
        )
        val savedToken = tokenRepository.save(token)
        logger.info("Token ${token.value} for user ${user.username} created")
        try {
            mailSender.send(fun (mimeMessage) {
                val messageHelper = MimeMessageHelper(mimeMessage)
                messageHelper.setFrom(address)
                messageHelper.setTo(user.email)
                messageHelper.setSubject(subject)

                val context = Context()
                context.setVariable(MAIL_TEMPLATE_URL, "$apiUrl/users/activate/${savedToken.value}")
                context.setVariable(MAIL_TEMPLATE_USERNAME, user.username)
                val message = templateEngine.process(MAIL_TEMPLATE_NAME, context)
                messageHelper.setText(message)
            })
            logger.info("Token ${token.value} sent to ${user.username}")
        } catch (exception: MailException) {
            logger.error("Unable to send mail to ${user.username}", exception)
        }
    }
}