plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    // R-38: Kotlin은 모든 class 기본 final → Spring @Configuration이 CGLIB proxy
    // 만들 때 실패 ("Configuration class may not be final"). plugin.spring이 자동으로
    // @Configuration/@Component/@Service/@Repository/@Controller 클래스에 `open`
    // 부여. consumer 측에서 final로 컴파일된 .class를 unfinal로 못 만드므로
    // 본 모듈이 source에서 직접 해결해야 함 (publish된 jar의 .class가 non-final).
    kotlin("plugin.spring")
}

dependencies {
    // common 모듈은 ResponseEntity + HttpStatus 만 사용 (common/exception/CustomException.kt).
    // 둘 다 `spring-web` artifact 에 있음 (servlet + webflux 공통).
    //
    // 이전: `spring-boot-starter-web` (servlet 전체 - spring-mvc + tomcat 포함) →
    //       consumer 가 reactive 면 classpath 충돌:
    //         - Spring Cloud Gateway: "Spring MVC found on classpath, which is
    //           incompatible with Spring Cloud Gateway"
    //         - WebSecurityConfiguration vs WebFluxSecurityConfiguration bean 중복
    //       이 issue 는 msa-api-gateway (SCG = reactive) 에서 발생.
    //
    // 본 변경: `spring-web` 만. spring-mvc + tomcat 안 가져옴.
    //         consumer side:
    //           - 5 service (auth/user/product/order/inventory): 자기 build.gradle.kts 에
    //             `spring-boot-starter-web` 직접 명시 (grep 검증). 영향 0.
    //           - msa-api-gateway: 자기 build 에 `spring-boot-starter-webflux` 명시.
    //             공통 `spring-web` API + reactive runtime → 정합.
    //
    // `api` configuration: ResponseEntity 같은 public API 가 consumer compile classpath
    // 에 노출되어야 하므로 `implementation` 보다 `api` 가 적절 (java-library plugin).
    api("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("com.h2database:h2")

    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("jakarta.annotation:jakarta.annotation-api")

    // R-57: 단위 테스트 — JUnit 5 + AssertJ + Mockito (spring-boot-starter-test BOM)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Gradle 8.x + JUnit Platform 1.12+ 에서 OutputDirectoryProvider 가 launcher 측에 있어
    // 명시적 testRuntimeOnly 필요. 없으면 "TestEngine with ID 'junit-jupiter' failed to discover tests" 발생.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        kotlin.srcDir("build/generated/source/kapt/main")
    }
}
