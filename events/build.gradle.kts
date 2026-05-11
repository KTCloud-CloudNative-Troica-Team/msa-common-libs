import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.5"
}

object Versions {
    // Monorepo의 grpc/protobuf 버전과 정렬:
    //   - protobuf-java/protobuf-kotlin: 4.34.1
    //   - protoc: 4.34.1 (com.google.protobuf:protoc:4.34.1)
    const val PROTOBUF = "4.34.1"
}

dependencies {
    api("com.google.protobuf:protobuf-java:${Versions.PROTOBUF}")
    api("com.google.protobuf:protobuf-kotlin:${Versions.PROTOBUF}")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java", "build/generated/source/proto/main/kotlin")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.PROTOBUF}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin")
            }
        }
    }
}
