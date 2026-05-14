package dev.ktcloud.black.common.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * R-57 (평가 기본 (3)-1): CustomException 단위 테스트.
 *
 * 모든 polyrepo 가 본 클래스를 base 로 도메인 예외 정의. toEntity() 가 클라이언트 응답
 * (ResponseEntity) 으로 변환하므로 핵심 동작 검증.
 */
@DisplayName("CustomException - 공통 예외 base 동작")
class CustomExceptionTest {

    @Test
    @DisplayName("toEntity() 가 status, code, message 를 그대로 보존")
    fun `toEntity 결과는 ExceptionBody 와 상태코드 보존`() {
        val ex = CustomException(
            code = "ERR-001",
            message = "테스트 메시지",
            status = HttpStatus.BAD_REQUEST.value(),
        )

        val response = ex.toEntity()

        assertThat(response.statusCode.value()).isEqualTo(400)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.code).isEqualTo("ERR-001")
        assertThat(response.body!!.message).isEqualTo("테스트 메시지")
        assertThat(response.body!!.status).isEqualTo(400)
    }

    @Test
    @DisplayName("HttpStatus enum 생성자 사용 시 status 가 정수로 변환됨")
    fun `HttpStatus 생성자 변환`() {
        // protected 생성자라 하위 클래스로 검증
        class TestException : CustomException(
            code = "ERR-002",
            message = "내부 오류",
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            throwable = null,
        )

        val ex = TestException()

        assertThat(ex.status).isEqualTo(500)
        assertThat(ex.code).isEqualTo("ERR-002")
        assertThat(ex.message).isEqualTo("내부 오류")
    }

    @Test
    @DisplayName("throwable 을 받으면 cause 로 RuntimeException 에 전달되지 않으나 필드로 보존")
    fun `throwable 필드 보존`() {
        val cause = RuntimeException("원인")
        val ex = CustomException(
            code = "ERR-003",
            message = "wrapper",
            status = 500,
            throwable = cause,
        )

        assertThat(ex.throwable).isSameAs(cause)
        // RuntimeException(message) 호출만 했으므로 cause 는 null 이 정상
        assertThat(ex.cause).isNull()
    }
}
