package dev.ktcloud.black.common.domain.entity

import dev.ktcloud.black.common.util.time.now
import java.time.LocalDateTime

open class BaseDomainEntity (
    override var createdAt: LocalDateTime = now(),
    override var updatedAt: LocalDateTime = now(),
    override var deletedAt: LocalDateTime? = null,
): BaseEntity