package dev.ktcloud.black.common.domain.entity

import java.time.LocalDateTime

interface BaseEntity {
    var createdAt: LocalDateTime
    var updatedAt: LocalDateTime
    var deletedAt: LocalDateTime?
}