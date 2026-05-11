import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    kotlin("plugin.jpa") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    `maven-publish`
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

    // Spring Boot BOM 으로 의존성 버전을 모노레포(SB 3.3.0)와 일치시킨다.
    // 단, org.springframework.boot 플러그인은 적용하지 않는다 (라이브러리는 fat jar 만들 필요 없음).
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.0")
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
