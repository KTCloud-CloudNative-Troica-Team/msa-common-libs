package dev.ktcloud.black.common.util.time

import java.time.LocalDateTime

fun now(): LocalDateTime {
    return LocalDateTime.now(MicrosecondTruncatingClock.Instance)
}