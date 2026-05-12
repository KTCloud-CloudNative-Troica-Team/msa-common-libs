# msa-common-libs

Troica Market Service MSA polyrepo의 공용 라이브러리. **최신 stable: `v0.3.1`** (2026-05-13).

> 단일 진실의 원천 (SPEC + ADR): [TROICA_SPEC.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROICA_SPEC.md), [ADR](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/tree/main/docs/adr)
> 트러블슈팅: [TROUBLESHOOTING.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md)

---

## 빠른 시작 (5분, L1 — 빌드 + 로컬 publish)

### 사전 요구사항

- **Java 21** (Temurin 권장)
- **GitHub PAT** with `read:packages` (consumer 측에서 받아올 때 사용)

### 1. 로컬 빌드 + 테스트

```bash
./gradlew build
```

기대 결과:
```
BUILD SUCCESSFUL in 1m 30s
```
- 2개 모듈 (`common`, `events`) 컴파일
- Protobuf codegen (events 모듈)
- jar + sources-jar + javadoc-jar 생성

### 2. 로컬 Maven repository에 publish (개발 시)

```bash
./gradlew publishToMavenLocal
```

검증: `~/.m2/repository/com/troica/msa/{common,events}/<version>/`에 6개 artifact (jar × 2, sources × 2, javadoc × 2) 확인.

consumer 측 빌드 시 `repositories { mavenLocal() }` 추가 시 사용 가능.

### 3. GitHub Packages publish (release 시)

```bash
git tag v0.3.2
git push origin v0.3.2
```

`.github/workflows/publish.yml`이 tag push 시 자동 실행 — 2개 모듈을 GitHub Packages로 publish. tag 이름의 `v` 제거된 값을 version으로 사용 (예: `v0.3.2` → `0.3.2`).

검증: GitHub 레포 → Packages 탭에서 새 버전 확인.

---

## 모듈 (v0.3.1 기준)

| 모듈 | 좌표 | 내용 |
|------|------|------|
| `common` | `com.troica.msa:common:0.3.1` | JPA/QueryDSL 설정 (`QuerydslConfig` 등), Base 엔티티, 공통 예외, 시간/Backoff 유틸. **kotlin-spring plugin** 적용으로 @Configuration 자동 non-final |
| `events` | `com.troica.msa:events:0.3.1` | Kafka 토픽 페이로드 Protobuf 스키마 + 생성된 Java/Kotlin 클래스 |

### 폐기된 모듈 (v0.3.0 trim, ADR-0002)

- ~~`client-redis`~~ → JitPack `com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2`로 일원화
- ~~`client-ses`~~ → notification-service 폐기와 함께 dead code 정리 (ADR-0001)

---

## 빌드 환경 (고정)

| 항목 | 버전 | 이유 |
|---|---|---|
| Java | 21 | Spring Boot 3.5.x 권장 |
| Kotlin | **2.1.0** | 2.3.x는 `BuildUtilKt.clearJarCaches` 버그 (TROUBLESHOOTING §1.2) |
| Gradle | **8.10.2** | Kotlin 2.1과 페어 |
| Spring Boot BOM | 3.5.14 | CVE-2025-22235 + 3.5.x EOL 회피 (ADR 보강 예정) |

---

## Consumer 측 사용법

소비 서비스(`msa-product-service` 등)의 `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    // 로컬 개발 시 (publishToMavenLocal 후)
    mavenLocal()
    // GitHub Packages (stable)
    maven {
        url = uri("https://maven.pkg.github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user")?.toString()
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token")?.toString()
        }
    }
}

dependencies {
    implementation("com.troica.msa:common:0.3.1")
    implementation("com.troica.msa:events:0.3.1")        // Kafka producer/consumer 사용 시
}
```

### 인증 설정 (로컬 개발)

`~/.gradle/gradle.properties`:
```
gpr.user=<your-github-username>
gpr.token=<your-PAT-with-read:packages>
```

PAT 발급: GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens. 권한은 `read:packages` 만으로 충분.

### CI 환경 (GitHub Actions)

`${{ secrets.GITHUB_TOKEN }}`이 자동 발급되므로 추가 설정 불필요. 동일 organization 안의 packages는 같은 token으로 접근 가능.

---

## Kafka Protobuf 스키마

`events/src/main/proto/` 하위 `.proto` 파일이 Kafka 토픽과 1:1 대응 (ADR-0003 `wire format = JSON`이지만 schema 정의는 Protobuf):

| Topic | Proto message | Producer → Consumer |
|-------|---------------|---------------------|
| `order.pending`              | `troica.order.v1.OrderPending`              | order → inventory |
| `order.inventory-reserved`   | `troica.order.v1.OrderInventoryReserved`    | inventory → order |
| `order.confirmed`            | `troica.order.v1.OrderConfirmed`            | order → (subscribers TBD, ADR-0008) |
| `order.cancelled`            | `troica.order.v1.OrderCancelled`            | order → (subscribers TBD) |
| `notification.requested`     | `troica.notification.v1.NotificationRequested` | (legacy, notification 폐기됨 — ADR-0001) |

### 스키마 호환성 규칙

- **필드 추가는 항상 OK** — 새 field number, optional (proto3 default)
- **필드 번호 변경 금지** — wire compatibility 깨짐
- **enum 값 추가는 OK** — 단 unknown은 `*_UNSPECIFIED`로 fallback, 컨슈머가 처리
- **breaking change → major version bump** (`v0.x` → `v1.x`), `.proto` package도 `.v2`로 신규 작성 후 양립 기간 운영

---

## 의사결정

- [ADR-0002](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0002-client-libraries-distribution.md) — client-redis JitPack 분리 + client-ses 제거
- [ADR-0003](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0003-kafka-wire-format-json.md) — Kafka wire format = JSON
- [ADR-0004](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0004-kafka-topic-naming.md) — Kafka 토픽명 SPEC 표준

## 트러블슈팅

- **`BuildUtilKt.clearJarCaches` NoClassDefFoundError** → Kotlin 2.3.x 호환성 문제. 본 레포는 2.1.0 + Gradle 8.10.2로 고정. [TROUBLESHOOTING §1.2](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#12-kotlin-23x--gradle-9x-buildutilktclearjarcaches-noclassdeffounderror)
- **`@Configuration class may not be final`** → v0.3.1에서 해결됨 (`kotlin("plugin.spring")` 적용). [TROUBLESHOOTING §1.7](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#17-kotlin-configuration-class가-final--spring-cglib-proxy-실패-r-38)
- **GH Packages `Unauthorized`** → `gpr.user` / `gpr.token` 누락 또는 PAT 만료. `read:packages` scope 확인.
- **`0.x.0-SNAPSHOT` not found** → SNAPSHOT은 publish 안 됨. tag push로 stable 버전 사용.

## 릴리스 절차 요약

1. PR 머지 후 main 갱신:
   ```bash
   git checkout main && git pull
   ```
2. tag push:
   ```bash
   git tag v<new-version>
   git push origin v<new-version>
   ```
3. GitHub Actions → Publish workflow → 성공 확인
4. consumer 서비스의 `build.gradle.kts`에서 버전 bump → PR 진행
