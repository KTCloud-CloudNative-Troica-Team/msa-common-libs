plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
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
}

sourceSets {
    main {
        kotlin.srcDir("build/generated/source/kapt/main")
    }
}
