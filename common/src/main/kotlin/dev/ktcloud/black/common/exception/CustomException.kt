package dev.ktcloud.black.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

open class CustomException(
    val code: String,
    override val message: String,
    val status: Int,
    val throwable: Throwable? = null
): RuntimeException(message) {
    protected constructor(
        code: String,
        message: String,
        status: HttpStatus,
        throwable: Throwable?
    ): this(code, message, status.value(), throwable)

    fun toEntity(): ResponseEntity<ExceptionBody> =
        ResponseEntity.status(status).body(ExceptionBody(code = code, message = message, status = status))

    data class ExceptionBody(
        val code: String,
        val message: String,
        val status: Int,
    )
}