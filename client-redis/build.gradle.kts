plugins {
    kotlin("kapt")
}

object Versions {
    const val REDISSON = "3.27.2"
    const val COROUTINE = "1.8.0"
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    api("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("org.redisson:redisson-spring-boot-starter:${Versions.REDISSON}")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINE}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.COROUTINE}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.COROUTINE}")
}
