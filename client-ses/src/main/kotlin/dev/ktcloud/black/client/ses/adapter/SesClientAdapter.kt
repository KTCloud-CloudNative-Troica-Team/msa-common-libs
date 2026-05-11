package dev.ktcloud.black.client.ses.adapter

import dev.ktcloud.black.client.ses.configuration.SesProperties
import dev.ktcloud.black.client.ses.core.SendMailUseCase
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class SesClientAdapter(
    private val mailSender: MailSender,
    private val sesProperties: SesProperties
): SendMailUseCase {
    override fun sendMail(param: SendMailUseCase.In) {
        val message = SimpleMailMessage().apply {
            from = sesProperties.senderEmail
            setTo(param.mailTo)
            subject = param.title
            text = param.content
        }

        mailSender.send(message)
    }
}