package dev.ktcloud.black.client.ses.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cloud.aws.ses")
data class SesProperties(
    @Value("source") val senderEmail: String
)