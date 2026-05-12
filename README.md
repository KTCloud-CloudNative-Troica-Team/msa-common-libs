# msa-common-libs

Troica Market Service MSA polyrepo의 공용 라이브러리.

> Single source of truth: [TROICA_SPEC.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/TROICA_SPEC.md)

## 모듈 (v0.3.0)

| 모듈 | 좌표 | 내용 |
|------|------|------|
| `common`       | `com.troica.msa:common`       | JPA/QueryDSL 설정, Base 엔티티, 공통 예외, 시간/Backoff 유틸 |
| `events`       | `com.troica.msa:events`       | Kafka 토픽 페이로드 Protobuf 스키마 + 생성된 Java/Kotlin 클래스 |

### v0.2.0에서 제거된 모듈 (2026-05-12 D2/D3 결정)

- ~~`client-redis`~~ — 팀장님의 JitPack 패키지 `com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2`로 일원화
- ~~`client-ses`~~ — notification-service 폐기와 함께 dead code 정리

## 직렬화 결정

**Protobuf (no schema registry for PoC).** Avro 미사용.

근거:
- 모노레포가 이미 gRPC + Protobuf 사용 중 → 도구·지식 재사용
- 2026년 기준 Confluent/Apicurio Registry 모두 Protobuf first-class 지원
- 동일 generated class를 모든 서비스가 의존성으로 공유 → 별도 registry 없이 SemVer로 호환 관리
- 추후 멀티팀 거버넌스 필요 시 Apicurio Registry 추가 (오픈소스, K8s Operator)

## 빌드

```bash
./gradlew build
./gradlew publishToMavenLocal     # ~/.m2 로컬 publish (개발용)
```

## 소비 (Consumer 측 build.gradle.kts)

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user")?.toString()
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token")?.toString()
        }
    }
}

dependencies {
    implementation("com.troica.msa:common:0.3.0")
    implementation("com.troica.msa:events:0.3.0")        // Kafka producer/consumer (코드 양립용; wire는 JSON)
}

// Redis 분산락/멱등 필요 서비스 (inventory, auth)는 JitPack 사용:
//   repositories { maven { url = uri("https://jitpack.io") } }
//   implementation("com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2")
```

로컬 개발 시 `~/.gradle/gradle.properties`에 `gpr.user`, `gpr.token` (packages:read 권한 PAT) 등록.

## 릴리스 (Publish)

```bash
git tag v0.1.0
git push origin v0.1.0
```

`.github/workflows/publish.yml`이 4개 모듈을 GitHub Packages로 push.

## Protobuf 스키마

`events/src/main/proto/` 하위 `.proto` 파일이 Kafka 토픽과 1:1 대응:

| Topic | Proto message | Producer → Consumer |
|-------|---------------|---------------------|
| `order.pending`              | `troica.order.v1.OrderPending`              | order → inventory |
| `order.inventory-reserved`   | `troica.order.v1.OrderInventoryReserved`    | inventory → order |
| `order.confirmed`            | `troica.order.v1.OrderConfirmed`            | order → notification |
| `order.cancelled`            | `troica.order.v1.OrderCancelled`            | order → notification, inventory(보상) |
| `notification.requested`     | `troica.notification.v1.NotificationRequested` | any → notification |

### 스키마 호환성 규칙

- **필드 추가는 항상 OK** — 새 field number, optional (proto3 default)
- **필드 번호 변경 금지** — wire compatibility 깨짐
- **enum 값 추가는 OK** — 단 unknown은 `*_UNSPECIFIED`로 fallback, 컨슈머가 처리
- **breaking change → major version bump** (`v0.x` → `v1.x`), `.proto` package도 `.v2`로 신규 작성 후 양립 기간 운영
