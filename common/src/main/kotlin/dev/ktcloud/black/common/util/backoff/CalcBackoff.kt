package dev.ktcloud.black.common.util.backoff

import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.pow

fun getNextRetryDateTime(
    retryCount: Int,
    baseTime: Long = 2,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
): LocalDateTime {
    val currentTime = LocalDateTime.now()

    val maxDelay = baseTime * 2.0.pow(retryCount.toDouble()).toLong()

    val randomDelay = ThreadLocalRandom.current().nextLong(baseTime, maxDelay + 1)

    return when (timeUnit) {
        TimeUnit.SECONDS -> currentTime.plusSeconds(randomDelay)
        TimeUnit.MINUTES -> currentTime.plusMinutes(randomDelay)
        TimeUnit.HOURS -> currentTime.plusHours(randomDelay)
        else -> throw IllegalArgumentException()
    }
}