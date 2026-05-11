package dev.ktcloud.black.client.ses.core

interface SendMailUseCase {
    fun sendMail(param: In)

    data class In(
        val mailTo: String,
        val title: String,
        val content: String
    )
}