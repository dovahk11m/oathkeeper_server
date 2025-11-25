# [최종] 1125_Python_AI_API_v2_연동_작업기록

- **작성일:** 2025-11-25
- **주요 목표:** Python AI 서버의 신규 API(v2) 명세에 맞춰, Spring 서버의 그룹 요약 기능 관련 코드를 리팩토링하고 안정적으로 연동한다.
- **최종 상태:** ✅ **완료**

---

## 1. 작업 계획

### 1.1. 배경
- Python AI 서버 팀에서 그룹 통계/요약 API를 v2 스펙으로 업데이트하고 회신함.
- 기존에 2개로 분리되어 있던 API가 **1개의 통합 API**로 변경되었으며, 요청/응답 DTO 구조 또한 변경됨.
- Spring 서버의 관련 로직을 v2 스펙에 맞춰 수정이 필요함.

### 1.2. 세부 계획
1.  **DTO 리팩토링:** AI 서버 v2의 JSON 구조에 매핑되는 DTO 클래스 재구성.
2.  **서비스 로직 수정:** `MetricsGroupService`의 AI 서버 호출 로직을 v2 스펙에 맞게 변경.
3.  **네트워크 설정 검토:** AI 서버의 긴 응답 시간을 고려하여 통신 타임아웃 설정 조정.
4.  **테스트 및 검증:** 변경된 로직이 실제 환경에서 정상 동작하는지 검증.

---

## 2. 작업 진행 및 산출물

### 2.1. DTO 리팩토링 (완료)
- **내용:** Python AI 서버 v2 명세에 따라, 그룹 요약 기능의 서버 통신에 필요한 DTO 클래스들을 `com.oath.domain.metrics.dto` 패키지 내에 재구성함. `@JsonProperty`를 사용하여 `snake_case`와 `camelCase`를 매핑함.
- **산출물:**
    - `AiGroupSummaryStats.java`
    - `AiGroupSummaryResponse.java`
    - `AiGroupSummaryRequest.java`

### 2.2. 서비스 로직 수정 (완료)
- **내용:** `MetricsGroupService`가 새로운 DTO를 사용하여 v2 스펙에 맞춰 통신하도록 수정하고, `warnings` 로깅 로직을 추가함.
- **산출물:** `MetricsGroupService.java` (수정)

### 2.3. 네트워크 설정 (완료)
- **내용:** 기존 `WebClient` 방식에서, 범용성과 안정성이 더 높은 `java.net.http.HttpClient`를 Bean으로 등록하여 사용하는 방식으로 변경함. 연결 및 요청 타임아웃을 설정하여 안정성을 확보함.
- **산출물:**
    - `HttpClientConfig.java` (신규)
    - `MetricsGroupService.java` (수정)

---

## 3. 연동 테스트 및 트러블슈팅

- **목표:** 개발된 그룹 AI 요약 기능을 테스트하고, Python AI 서버와 실제 통신이 성공하는지 확인.

### 3.1. 문제 발생: 401 Unauthorized & 404 Not Found
- **현상 1 (401 Unauthorized):** Swagger UI에서 API 호출 시, `Authorization` 헤더가 누락되어 401 에러 발생. `curl` 테스트는 정상.
- **현상 2 (404 Not Found):** 서버 시작 시 `DataInitializer`에서 AI 서버 호출 시, AI 서버 로그에 `POST / HTTP/1.1" 404 Not Found`가 기록됨.

### 3.2. 해결 과정
1.  **`JwtTokenProvider` 및 `AuthInterceptor` 안정성 강화:** `null` 또는 빈 토큰이 전달될 경우 `NullPointerException`이 발생하는 것을 방지하기 위해, 두 클래스에 `null` 및 `isBlank` 체크 로직을 추가함.
2.  **`GroupController`의 `@SecurityRequirement` 추가:** `PlanRestController`와의 비교를 통해, `GroupController` 클래스 레벨에 `@SecurityRequirement(name = "Bearer Authentication")` 어노테이션이 누락되었음을 발견하고 추가함. 이로써 Swagger UI가 인증 헤더를 정상적으로 포함하도록 유도함.
3.  **`MetricsGroupService`의 요청 URI 수정:** `HttpRequest` 생성 시, `aiServerBaseUrl`에 엔드포인트 경로(`/metrics/group/summary`)가 올바르게 조합되도록 수정함.

### 3.3. 최종 결과
- 상기 조치 후, Swagger UI를 통한 API 호출 및 서버 시작 시 `DataInitializer`를 통한 비동기 호출 모두 **정상적으로 Python AI 서버와 통신하여 요약 결과를 받아오는 것을 확인**함.
- **결론:** Python AI 서버 API v2 연동이 성공적으로 완료됨.

---

## 4. 최종 교훈 및 요약

1.  **`curl` vs UI 도구:** `curl`과 같은 로우 레벨(low-level) 도구에서 API가 정상 작동한다면, 서버의 핵심 로직보다는 클라이언트나 중간 설정(Swagger UI 등)의 문제일 가능성이 높다.
2.  **Swagger 어노테이션의 중요성:** `@SecurityRequirement`는 API 문서화 및 테스트 편의성에 결정적인 역할을 한다. 인증이 필요한 컨트롤러에는 반드시 추가하여 일관성을 유지해야 한다.
3.  **방어적 프로그래밍:** 클라이언트가 언제나 올바른 요청을 보낼 것이라고 가정해서는 안 되며, 외부 입력을 받는 경계에는 항상 방어 코드를 추가하여 서버의 안정성을 높여야 한다.
