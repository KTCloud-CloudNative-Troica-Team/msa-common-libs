package dev.ktcloud.black.client.redis.core

import dev.ktcloud.black.client.redis.api.DistributedLock
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Aspect
@Component
class IdempotentEventAspect(
    private val distributedLock: DistributedLock,
    private val redissonClient: RedissonClient
) {
    companion object {
        private const val IDEMPOTENT_PROCESS_KEY_PREFIX = "idempotent-processed"
    }
}