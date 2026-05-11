package dev.ktcloud.black.common.adapter.infrastructure.jpa

import dev.ktcloud.black.common.domain.entity.BaseEntity

interface EntityMapper<BasePersistenceEntity: BaseEntity, DomainEntity> {
    fun toOrmEntity(domainEntity: DomainEntity): BasePersistenceEntity
    fun toOrmEntity(domainEntities: List<DomainEntity>): List<BasePersistenceEntity> = domainEntities.map { toOrmEntity(it) }

    fun toDomainEntity(entity: BasePersistenceEntity): DomainEntity
    fun toDomainEntity(entities: List<BasePersistenceEntity>): List<DomainEntity> = entities.map { toDomainEntity(it) }
    fun toDomainEntity(entities: Iterable<BasePersistenceEntity>): List<DomainEntity> = entities.map { toDomainEntity(it) }
}