import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    kotlin("plugin.jpa") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    `maven-publish`

    // R-45 (평가 심화 (2)-1): SonarCloud 정적 분석 + 커버리지 게이트.
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "ktcloud-cloudnative-troica-team")
        property("sonar.projectKey", "KTCloud-CloudNative-Troica-Team_msa-common-libs")
        property("sonar.qualitygate.wait", "true")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "**/build/reports/jacoco/test/jacocoTestReport.xml",
        )
        // Protobuf 코드젠 + build 산출물 제외 (common-libs:events 모듈)
        property("sonar.exclusions", "**/generated/**, **/build/**, **/proto/**")
    }
}

allprojects {
    group = "com.troica.msa"
    version = providers.gradleProperty("version").get()

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    // R-45: JaCoCo로 커버리지 측정 → SonarCloud가 XML report 읽음.
    apply(plugin = "jacoco")

    // Spring Boot BOM 으로 의존성 버전을 통일 (사진 "기술 스택" 표준: SB 3.5.x).
    // 3.5.13 = 2026-03-26 release, Spring Cloud 2025.0.2가 공식 테스트한 짝. 3.5 OSS support는 2026-06-30 EOL 예정.
    // org.springframework.boot 플러그인은 적용하지 않는다 (라이브러리는 bootJar 불필요).
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.13")
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
        jvmToolchain(21)
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    }

    // kapt가 build/generated/source/kapt/main 에 쓰고 sourceSets가 그것을 입력으로 잡으므로
    // sourcesJar는 kaptKotlin 결과를 입력으로 받게 된다. Gradle 8.10의 input validation을 만족시키기 위해
    // 명시적 dependsOn.
    plugins.withId("org.jetbrains.kotlin.kapt") {
        tasks.matching { it.name == "sourcesJar" }.configureEach {
            dependsOn("kaptKotlin")
        }
    }

    // R-45: test → jacocoTestReport → Sonar XML report 자동 생성.
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.matching { it.name == "jacocoTestReport" })
    }
    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    extensions.configure<PublishingExtension> {
        publications {
            register<MavenPublication>("library") {
                from(components["java"])
                artifactId = project.name
                pom {
                    name.set("${project.group}:${project.name}")
                    description.set("Troica MSA common library: ${project.name}")
                    url.set("https://github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/license/mit/")
                        }
                    }
                    scm {
                        url.set("https://github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                        ?: providers.gradleProperty("gpr.user").orNull
                    password = System.getenv("GITHUB_TOKEN")
                        ?: providers.gradleProperty("gpr.token").orNull
                }
            }
        }
    }
}
