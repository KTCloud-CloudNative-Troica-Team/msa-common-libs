package dev.ktcloud.black.common.util.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MicrosecondTruncatingClock private constructor(private val delegate: Clock = systemDefaultZone()): Clock() {
    override fun getZone(): ZoneId = delegate.zone
    override fun withZone(zone: ZoneId): Clock = MicrosecondTruncatingClock(delegate.withZone(zone))

    override fun instant(): Instant {
        val instant = delegate.instant()
        val truncatedNanos = (instant.nano / 1_000) * 1_000
        return Instant.ofEpochSecond(instant.epochSecond, truncatedNanos.toLong())
    }

    companion object {
        val Instance = MicrosecondTruncatingClock()
    }
}