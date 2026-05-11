package dev.ktcloud.black.client.redis.api

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IdempotentEventProcessor(
    private val distributedLock: DistributedLock,
    private val redissonClient: RedissonClient
) {
    companion object {
        private const val IDEMPOTENT_PROCESS_KEY_PREFIX = "idempotent-processed"
    }

    fun <R> withIdempotencyProcess(
        key: String,
        ttl: Duration = Duration.ofMinutes(30),
        func: () -> R
    ): R? {
        val idempotentKey = "$IDEMPOTENT_PROCESS_KEY_PREFIX$key"

        val bucket = redissonClient.getBucket<String>(idempotentKey)

        if (bucket.get() == "DONE") return null

        return distributedLock.execute(key, func = {
                if (bucket.get() == "DONE") return@execute null

                val result = func.invoke()

                bucket.set("DONE", ttl)

                result
            }
        )
    }
}