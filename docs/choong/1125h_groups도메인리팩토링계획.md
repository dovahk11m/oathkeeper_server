# 1125h_그룹 도메인 리팩토링 계획 및 현황

## 1. 문제 정의 및 초기 현황 (2025-11-25)

그룹 AI 요약 기능 구현 과정에서 다음과 같은 주요 문제가 발생했습니다.

*   **🚨 401 Unauthorized 에러:** `GET /api/groups/{groupId}/metrics/summary` 엔드포인트 호출 시 `Authorization` 헤더가 누락되어 발생.
*   **🐍 Python AI 서버 404 Not Found:** Spring 서버가 Python AI 서버의 잘못된 엔드포인트로 요청을 보내는 문제.
*   **📝 그룹 요약 `null` 저장:** AI 서버로부터 요약 텍스트를 성공적으로 받았으나, DB에 `null`로 저장되는 문제.
*   **🔄 자동화 로직 미작동:** 약속 완료 시 그룹 요약이 자동으로 트리거되지 않는 문제.
*   **❌ 직접 요청 시 `summary:null`:** `GET /api/groups/{groupId}/metrics/summary` 직접 호출 시 DB에 요약이 저장되었음에도 `null`로 응답되는 문제.

## 2. 해결된 문제 및 적용된 수정사항

현재까지 다음 문제들이 해결되었거나, 해결을 위한 코드가 적용되었습니다.

1.  **401 Unauthorized 에러 (클라이언트 측 문제):**
    *   **원인:** `Invoke-WebRequest` 등 클라이언트에서 `Authorization: Bearer <TOKEN>` 헤더를 유효한 토큰과 함께 전송하지 않아 발생.
    *   **해결:** 클라이언트 측에서 유효한 JWT 토큰을 올바른 형식으로 헤더에 포함하여 요청하도록 안내. (서버 코드 수정 사항 없음)

2.  **Python AI 서버 404 Not Found:**
    *   **원인:** `Invoke-WebRequest` 명령어 사용 시 `{groupId}` 플레이스홀더를 실제 ID로 교체하지 않아 발생한 `MethodArgumentTypeMismatchException`이 선행되었고, 이후 `MetricsGroupService`에서 AI 서버로 요청하는 URI 구성이 잘못되었을 가능성이 제기되었으나, 실제로는 `ai.server.url` 설정과 `/metrics/group/summary` 경로 조합은 올바르게 되어 있었음.
    *   **해결:** `Invoke-WebRequest` 명령어에서 `{groupId}`를 실제 그룹 ID(숫자)로 교체하여 요청하도록 수정.

3.  **그룹 요약 `null` 저장 (`Summary: null` 문제):**
    *   **원인:**
        *   Python AI 서버의 응답 JSON 구조 (`data` 객체 안에 `text_summary` 필드)와 Spring의 `AiGroupSummaryResponse` DTO 구조가 불일치.
        *   `MetricsGroupService`에서 `AiGroupSummaryResponse` DTO로부터 요약 텍스트를 추출하는 로직이 잘못됨 (`aiResponse.getSummary()` 호출).
        *   `AiGroupSummaryResponse.ResponseData` 내부 클래스에 `@Getter` 어노테이션 누락.
    *   **해결:**
        *   `AiGroupSummaryResponse.java` DTO를 Python AI 서버 스펙에 맞춰 `ResponseData` 내부 클래스와 `text_summary` 필드를 포함하도록 수정.
        *   `AiGroupSummaryResponse.ResponseData`에 `@Getter` 어노테이션 추가.
        *   `MetricsGroupService.java`에서 요약 텍스트를 `aiResponse.getData().getText_summary()`로 추출하도록 수정.

4.  **자동화 로직 미작동 (그룹 요약 트리거):**
    *   **원인:** 약속(Plan)이 완료될 때 그룹 요약 생성을 트리거하는 로직이 부재.
    *   **해결:**
        *   `PlanCompletedEvent` (planId, groupId 포함) 정의.
        *   `PlanService.java`의 `completePlan` 및 `PlanTrackingService.java`의 `checkAndCompletePlan`, `completePlanManually` 메서드에서 `Plan` 완료 후 `PlanCompletedEvent` 발행 로직 추가.
        *   `PlanCompletionListener.java`를 생성하여 `PlanCompletedEvent`를 수신하고, `MetricsGroupService.requestGroupSummary(groupId)`를 비동기적으로 호출하도록 구현.

5.  **직접 요청 시 `summary:null`:**
    *   **원인:** `GroupController.getGroupMetricsSummary` 메서드가 비동기적으로 업데이트된 `Group` 엔티티의 최신 정보를 데이터베이스에서 가져오지 못하고, 캐시된 이전 상태를 참조하여 `null`로 응답.
    *   **해결:** `GroupController.getGroupMetricsSummary` 메서드에 `@Transactional(readOnly = true)` 어노테이션을 추가하여 항상 새로운 읽기 전용 트랜잭션 내에서 최신 데이터를 조회하도록 수정.

## 3. 향후 리팩토링 및 개선 계획 (To-Do)

현재까지의 수정으로 그룹 AI 요약 기능의 핵심 동작은 정상화되었으나, 코드 검토 결과 다음과 같은 잠재적 결함 및 개선점이 발견되었습니다.

1.  **`AiGroupSummaryRequest.java` DTO 스펙 불일치:**
    *   **현황:** 현재 DTO는 `groupId` 필드를 포함하고 `style`, `notes`, `mode` 필드가 없으며, `planIds` 필드명이 스펙(`plan_ids`)과 다름.
    *   **계획:**
        *   `groupId` 필드 제거.
        *   `style`, `notes`, `mode` 필드 추가.
        *   `planIds` 필드에 `@JsonProperty("plan_ids")` 어노테이션을 사용하여 JSON 필드명을 명시적으로 지정.
        *   `MetricsGroupService.java`에서 `AiGroupSummaryRequest` 객체 생성 시, 수정된 DTO에 맞춰 `groupId`를 설정하지 않고 `style`, `notes`, `mode` 필드에 대한 값을 설정하는 로직 추가 (기본값 또는 설정 가능한 값).

2.  **`GroupService.java`의 `getGroupMembers` 메서드 권한 검증 누락 (🚨 중요 결함):**
    *   **현황:** `TODO` 주석으로 명시되어 있으며, 현재 그룹 멤버가 아닌 사용자도 특정 그룹의 멤버 목록을 조회할 수 있는 보안 취약점이 존재.
    *   **계획:** `getGroupMembers` 메서드 시작 부분에서 요청자가 해당 그룹의 멤버인지 확인하는 권한 검증 로직 (`validateGroupMember(groupId, requesterId)`)을 반드시 추가.

3.  **`GroupService.java`의 `getMyGroups` 메서드 N+1 쿼리 가능성:**
    *   **현황:** `PageResponseDTO.from` 람다 내부에서 `chatRepository.findTopByGroupIdOrderBySentAtDesc(group.getId())`가 호출되어 N+1 쿼리 발생 가능성.
    *   **계획:** 한 번의 쿼리로 모든 그룹의 마지막 메시지를 가져온 후 맵 형태로 저장하여 람다에서 조회하는 방식으로 최적화 고려.

4.  **`GroupService.java`의 `leaveGroup` 메서드 최적화:**
    *   **현황:** `countByGroupId` 호출과 `groupMemberRepository.delete(groupMember.getGroup())` 로직에서 불필요한 엔티티 로딩 및 추가 쿼리 발생 가능성.
    *   **계획:** `groupRepository.deleteById(groupId)`를 사용하거나, 쿼리 최적화를 통해 성능 개선 고려.

5.  **`MetricsGroupService.java`의 `HttpClient` 인스턴스 재사용:**
    *   **현황:** `requestGroupSummary` 메서드 호출 시마다 새로운 `HttpClient` 인스턴스 생성.
    *   **계획:** `HttpClient`를 `@Bean`으로 등록하여 한 번만 생성하고 주입받아 사용하도록 수정하여 성능 및 리소스 효율성 개선.

6.  **`MetricsGroupService.java`의 예외 처리 구체화:**
    *   **현황:** `RuntimeException` 대신 애플리케이션의 커스텀 예외 (`Exception404`)를 사용하는 것이 일관성 및 에러 핸들링에 더 좋음.
    *   **계획:** `groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));` 부분을 `Exception404("Group not found")`로 변경.

---

**클라이언트 측 문제 재강조:**

*   `GET /api/groups/{groupId}/metrics/summary` 엔드포인트를 직접 호출할 때 발생하는 `401 Unauthorized` 에러는 Spring 서버의 코드 문제가 아닌, **클라이언트(Postman, Swagger UI, Invoke-WebRequest 등)가 유효한 JWT 토큰을 `Authorization: Bearer <TOKEN>` 형식으로 요청 헤더에 포함하지 않아서 발생하는 문제**입니다. 이 부분은 클라이언트 측에서 항상 유의해야 합니다.
