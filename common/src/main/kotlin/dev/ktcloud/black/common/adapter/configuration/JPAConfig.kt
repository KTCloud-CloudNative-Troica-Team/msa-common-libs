package dev.ktcloud.black.common.adapter.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["dev.ktcloud.black.**.adapter.infrastructure.jpa.repository"],
)
@EntityScan(
    basePackages = [
        "dev.ktcloud.black.common.domain.entity",
        "dev.ktcloud.black.**.adapter.infrastructure.jpa.entity",
    ],
)
class JpaConfig