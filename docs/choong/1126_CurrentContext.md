# Current Problem Context (2025-11-26 기준)

- **문서 목적:** 본 문서는 새 작업자가 현재 프로젝트의 맥락을 빠르게 파악하고, **즉시 다음 작업을 이어받아 수행할 수 있도록** 현재 직면한 문제와 다음 목표에 대한 핵심 정보를 제공합니다.
- **현재 상태:** 기존 '그룹 AI 요약 기능'의 401 에러 문제는 해결되었으며, 현재의 주요 목표는 이관되었던 **'약속 중간 저장 문제'**와 **'실시간 지도 기능 연동'** 입니다.

---

## 1. 최근 해결된 주요 이슈 (참고용)

- **이슈:** Swagger UI에서만 `Authorization` 헤더가 누락되어 401 에러 발생.
- **해결:** `GroupController`에 `@SecurityRequirement` 어노테이션을 추가하여 Swagger UI가 인증 헤더를 정상적으로 전송하도록 수정함. 이 과정에서 `JwtTokenProvider`의 안정성 강화 및 `HttpClient` 리팩토링 등 코드 품질 개선 작업을 병행함.
- **교훈:** `curl` 등에서는 정상이지만 UI 도구에서 문제가 발생할 경우, 해당 도구의 설정(`@SecurityRequirement` 등)을 우선적으로 의심해야 한다.

---

## 2. 현재 최우선 문제: 약속 중간 저장 기능 오류

- **문제 정의:** `Plan`, `Participant` 엔티티 변경 이후, 클라이언트에서 약속을 생성하거나 수정할 때 데이터가 정상적으로 저장되지 않는 문제가 발생하고 있습니다.
- **추정 원인:** 서버와 클라이언트 간의 데이터 전송 객체(DTO) 불일치 또는 새로 추가된 Enum(`MovementStatus` 등) 처리 로직의 문제로 추정됩니다.
- **다음 작업 (Next Step):**
    1.  **서버-클라이언트 DTO 비교:** 클라이언트가 전송하는 JSON 데이터 구조와 서버의 `PlanRequest` DTO 필드가 정확히 일치하는지 비교/확인해야 합니다.
    2.  **핵심 로직 디버깅:** `PlanFacade`의 `createPlan`, `updatePlan` 메서드에 브레이크포인트를 설정하고, 실제 요청 시 데이터가 어떻게 변환되고 저장되는지 단계별로 추적해야 합니다.

---

## 3. 다음 목표: 실시간 지도 기능 클라이언트 연동

- **목표:** 클라이언트 개발자와 협업하여 실시간 지도 기능의 연동을 완료하고 테스트 환경을 구축합니다.
- **현재 상태:** 서버 측 WebSocket 및 관련 API는 준비되었으나, 클라이언트 연동이 지연되고 있습니다.
- **다음 작업 (Next Step):**
    1.  **클라이언트 연동 현황 파악:** 클라이언트 개발자와 소통하여 `LIVE_MAP_API.md` 기반의 구현 진행 상황 및 어려움을 파악해야 합니다.
    2.  **테스트용 API 구현:** 클라이언트 개발의 편의성을 위해, 특정 참가자의 상태(도착, 정체 등)를 강제로 변경할 수 있는 테스트 API(`PlanTestApiController`에 구현)를 우선적으로 제공해야 합니다.

---

## 4. 주요 파일 경로

### 4.1. 약속 저장 관련 파일
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/controller/PlanRestController.java`
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/facade/PlanFacade.java`
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/service/PlanService.java`
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/request/PlanRequest.java` (클라이언트 요청 DTO)
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/domain/Plan.java` (엔티티)
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/domain/Participant.java` (엔티티)

### 4.2. 실시간 지도 관련 파일
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/controller/PlanTestApiController.java` (테스트 API 추가 필요)
-   `C:/workspace/oath/src/main/java/com/oath/common/config/WebSocketConfig.java`
-   `C:/workspace/oath/src/main/java/com/oath/domain/plan/event/listener/ArrivalListener.java`
-   `C:/workspace/oath/docs/LIVE_MAP_API.md` (클라이언트 전달용 API 명세)
