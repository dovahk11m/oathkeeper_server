# Current Problem Context (2025-11-26 기준)

- **문서 목적:** 본 문서는 새 작업자가 현재 프로젝트의 맥락을 빠르게 파악하고, **즉시 다음 작업을 이어받아 수행할 수 있도록** 현재 직면한 문제와 다음 목표에 대한 핵심 정보를 제공합니다.
- 

## 0. 작업 규칙 및 코드 규칙

본 작업은 다음 규칙들을 준수합니다.

1.  **한글로만 소통:** 모든 의사소통은 한글로 진행합니다.
2.  **거절하면 즉시 작업 중지:** 요청에 대한 거절 의사가 있다면 즉시 작업을 중지합니다.
3.  **`@/docs/규칙_코딩컨벤션_by김근호.md` 준수:** 코딩 스타일 및 컨벤션을 따릅니다.
4.  **`@/docs/규칙_서비스_트랜잭션.md` 준수:** 서비스 트랜잭션 관련 규칙을 따릅니다.

---

## 1. 최근 해결된 주요 이슈 (참고용)

- **이슈:** Swagger UI에서만 `Authorization` 헤더가 누락되어 401 에러 발생.
- **해결:** `GroupController`에 `@SecurityRequirement` 어노테이션을 추가하여 Swagger UI가 인증 헤더를 정상적으로 전송하도록 수정함. 이 과정에서 `JwtTokenProvider`의 안정성 강화 및 `HttpClient` 리팩토링 등 코드 품질 개선 작업을 병행함.
- **교훈:** `curl` 등에서는 정상이지만 UI 도구에서 문제가 발생할 경우, 해당 도구의 설정(`@SecurityRequirement` 등)을 우선적으로 의심해야 한다.

---

## 2. 다음 목표: 실시간 지도 기능 클라이언트 연동

- **목표:** 클라이언트 개발자와 협업하여 실시간 지도 기능의 연동을 완료하고 테스트 환경을 구축합니다.
- **현재 상태:** 서버 측 WebSocket 및 관련 API는 준비되었으며, 클라이언트 개발 지원을 위한 테스트 API 구현도 완료되었습니다.
- **다음 작업 (Next Step):**
    1.  **클라이언트 연동 현황 파악:** 클라이언트 개발자와 소통하여 `LIVE_MAP_API.md` 기반의 구현 진행 상황 및 어려움을 파악해야 합니다.
    2.  **테스트용 API 구현 완료:** 클라이언트 개발의 편의성을 위해, 특정 참가자의 상태(`MovementStatus`)를 강제로 변경할 수 있는 테스트 API(`PlanTestApiController`)를 구현했습니다. (`/api/plans/{planId}/participants/{participantId}/test/force-movement-status`)

---

## 4. 주요 파일 경로

### 4.1. 주요 도메인 파일 (그룹 AI 요약 기능 관련)

*   **엔티티/Enum:**
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/Group.java`
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/SummaryStatus.java`
*   **리포지토리:**
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupRepository/GroupRepository.java`
    *   `C:/workspace/oath/src/main/java/com/oath/domain/plan/repository/PlanJpaRepository.java`
*   **서비스:**
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupService/GroupService.java`
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupService/MetricsGroupService.java`
*   **컨트롤러:**
    *   `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupController/GroupController.java`
*   **DTO (AI 통신용):**
    *   `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiGroupSummaryRequest.java`
    *   `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiGroupSummaryResponse.java`
    *   `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiGroupSummaryStats.java`
*   **초기화 데이터:**
    *   `C:/workspace/oath/src/main/java/com/oath/initializer/DataInitializer12_Plan.java`

### 4.2. 핵심 공용 파일 (인증/설정/HTTP 클라이언트 관련)

*   **인증/보안:**
    *   `C:/workspace/oath/src/main/java/com/oath/common/config/WebMvcConfig.java`
    *   `C:/workspace/oath/src/main/java/com/oath/common/auth/AuthInterceptor.java`
    *   `C:/workspace/oath/src/main/java/com/oath/common/JwtTokenProvider.java`
*   **HTTP 클라이언트:**
    *   `C:/workspace/oath/src/main/java/com/oath/common/config/WebClientConfig.java` (WebClient 설정 파일, 현재는 `java.net.http.HttpClient` 사용)
*   **설정:**
    *   `C:/workspace/oath/src/main/resources/application-local.yml`
    *   `C:/workspace/oath/build.gradle`


### 4.2. 실시간 지도 관련 파일
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/controller/PlanTestApiController.java` (테스트 API 추가 완료)
-   `C:/workspace/oath/src/main/java/com/oath/common/config/WebSocketConfig.java`
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/event/listener/ArrivalListener.java`
-   `C:/workspace/oath/docs/LIVE_MAP_API.md` (클라이언트 전달용 API 명세)
