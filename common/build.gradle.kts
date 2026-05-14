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
    // Web 의존성은 ResponseStatusException, ResponseEntity 같은 공통 예외 매핑에 사용 (common/exception)
    implementation("org.springframework.boot:spring-boot-starter-web")
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
