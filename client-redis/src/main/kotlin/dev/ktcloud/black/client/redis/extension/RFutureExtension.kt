package dev.ktcloud.black.client.redis.extension

import kotlinx.coroutines.suspendCancellableCoroutine
import org.redisson.api.RFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> RFuture<T>.awaitResponse(): T = suspendCancellableCoroutine { continuation ->
    this.onComplete { result, throwable ->
        if (throwable != null) {
            continuation.resumeWithException(throwable)
        } else {
            continuation.resume(result)
        }
    }

    continuation.invokeOnCancellation {
        this.cancel(true)
    }
}