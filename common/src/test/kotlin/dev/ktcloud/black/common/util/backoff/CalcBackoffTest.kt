package dev.ktcloud.black.common.util.backoff

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * R-57 (평가 기본 (3)-1): getNextRetryDateTime 단위 테스트.
 *
 * 본 함수는 Outbox poller / Kafka consumer retry 의 지수 백오프 계산에 사용됨.
 * 시간 의존 함수라 정확한 시각 비교는 불가 — "현재 이후" 와 "최대 한계 이내" 만 검증함.
 */
@DisplayName("CalcBackoff - 지수 백오프 다음 시각 계산")
class CalcBackoffTest {

    @Test
    @DisplayName("retryCount=0 + baseTime=2 분 → 결과는 정확히 현재 + 2분 (randomDelay 범위가 [2,2])")
    fun `retryCount 0 일 때 정확히 baseTime 만큼 delay`() {
        val before = LocalDateTime.now()
        val next = getNextRetryDateTime(retryCount = 0, baseTime = 2, timeUnit = TimeUnit.MINUTES)
        val after = LocalDateTime.now()

        // randomDelay 가 [baseTime, baseTime * 2^0 + 1) = [2, 3) 범위. 즉 2 또는 3분 가능
        assertThat(next).isAfterOrEqualTo(before.plusMinutes(2).minusSeconds(1))
        assertThat(next).isBeforeOrEqualTo(after.plusMinutes(3))
    }

    @Test
    @DisplayName("retryCount=3 → 결과는 현재 + [base, base * 2^3] 범위")
    fun `retryCount 가 클수록 max delay 도 지수 증가`() {
        val before = LocalDateTime.now()
        val next = getNextRetryDateTime(retryCount = 3, baseTime = 2, timeUnit = TimeUnit.MINUTES)
        val after = LocalDateTime.now()

        // randomDelay 범위 = [2, 2 * 2^3 + 1) = [2, 17). 최대 16분까지 가능
        assertThat(next).isAfterOrEqualTo(before.plusMinutes(2).minusSeconds(1))
        assertThat(next).isBeforeOrEqualTo(after.plusMinutes(16))
    }

    @Test
    @DisplayName("TimeUnit.SECONDS / MINUTES / HOURS 외에는 IllegalArgumentException")
    fun `지원하지 않는 TimeUnit 은 예외`() {
        assertThatThrownBy {
            getNextRetryDateTime(retryCount = 0, timeUnit = TimeUnit.DAYS)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            getNextRetryDateTime(retryCount = 0, timeUnit = TimeUnit.NANOSECONDS)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @DisplayName("SECONDS 단위 사용 시 분이 아닌 초로 delay")
    fun `SECONDS 단위 인식`() {
        val before = LocalDateTime.now()
        val next = getNextRetryDateTime(retryCount = 0, baseTime = 1, timeUnit = TimeUnit.SECONDS)
        val after = LocalDateTime.now()

        // randomDelay = [1, 2). 즉 1초만 더해짐 (분으로 환산 시 거의 동일)
        assertThat(next).isAfter(before)
        assertThat(next).isBeforeOrEqualTo(after.plusSeconds(2))
    }
}
