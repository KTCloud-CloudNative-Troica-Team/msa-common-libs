package dev.ktcloud.black.client.redis.api

import dev.ktcloud.black.client.redis.extension.awaitResponse
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Component
class DistributedLock(
    private val redissonClient: RedissonClient
) {
    fun <R> execute(
        key: String,
        func: () -> R,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        waitTime: Long = 5L,
        leaseTime: Long = 3L
    ): R {
        val lock = redissonClient.getLock(key)

        val available = lock.tryLock(waitTime, leaseTime, timeUnit)

        if (!available) throw TimeoutException("Redisson Lock timed out for key: $key")

        return try {
            func.invoke()
        } finally {
            lock.unlock()
        }
    }

    suspend fun <R> executeAsync(
        key: String,
        func: () -> R,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        waitTime: Long = 5L,
        leaseTime: Long = 3L
    ): R {
        val lock = redissonClient.getLock(key)

        val available = lock.tryLockAsync(waitTime, leaseTime, timeUnit).awaitResponse()

        if (!available) throw TimeoutException("Redisson Lock timed out for key: $key")

        return try {
            func.invoke()
        } finally {
            lock.unlockAsync()
        }
    }

    fun <R> executeNowOrFail(
        key: String,
        func: () -> R,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        leaseTime: Long = 3L,
    ): R {
        return try {
            execute(key, func, timeUnit, 0, leaseTime)
        } catch (_: TimeoutException) {
            throw IllegalStateException("Already Locked Key for key: $key")
        }
    }
}