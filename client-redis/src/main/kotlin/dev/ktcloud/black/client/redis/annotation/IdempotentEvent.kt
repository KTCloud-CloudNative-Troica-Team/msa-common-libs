package dev.ktcloud.black.client.redis.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class IdempotentEvent(
    val key: String,
    val ttlSeconds: Long = 10L
)
