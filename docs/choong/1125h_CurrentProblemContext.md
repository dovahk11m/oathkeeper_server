# 1125h_CurrentProblemContext: 그룹 AI 요약 기능 문제 현황 분석

- **작성일:** 2025-11-25
- **목표:** 현재 그룹 AI 요약 기능 구현 과정에서 발생하는 문제의 현황, 진행 상황, 그리고 다음 단계를 명확히 이해하기 위한 컨텍스트 제공. 새 대화에서 즉시 필요한 작업을 할 수 있도록 핵심 정보를 담습니다.

---

## 0. 작업 규칙 및 코드 규칙

본 작업은 다음 규칙들을 준수합니다.

1.  **한글로만 소통:** 모든 의사소통은 한글로 진행합니다.
2.  **거절하면 즉시 작업 중지:** 요청에 대한 거절 의사가 있다면 즉시 작업을 중지합니다.
3.  **`@/docs/규칙_코딩컨벤션_by김근호.md` 준수:** 코딩 스타일 및 컨벤션을 따릅니다.
4.  **`@/docs/규칙_서비스_트랜잭션.md` 준수:** 서비스 트랜잭션 관련 규칙을 따릅니다.

---

## 1. 문제 정의: 그룹 AI 요약 기능 미작동

Spring 서버에서 Python AI 서버로 그룹 요약을 요청하고, 그 결과를 DB에 저장하는 기능이 정상적으로 동작하지 않고 있습니다. 개별 약속(Plan) 요약은 정상 작동합니다.

---

## 2. 관찰된 문제 현상 및 로그 분석

현재까지의 로그 분석을 통해 다음과 같은 문제 현상이 관찰되었습니다.

### 2.1. 🚨 401 Unauthorized 에러 (가장 큰 블로커)

*   **로그:**
    ```
    2025-11-25T14:50:53.477+09:00 DEBUG 15848 --- [nio-8080-exec-9] com.oath.common.auth.AuthInterceptor     : Authorization header: null
    2025-11-25T14:50:53.477+09:00 DEBUG 15848 --- [nio-8080-exec-9] com.oath.common.auth.AuthInterceptor     : Resolved token: null
    2025-11-25T14:50:53.478+09:00 ERROR 15848 --- [nio-8080-exec-9] c.o.common.exception.MyExceptionHandler  : !!! 예상치 못한 런타임 에러 발생 !!!
    java.lang.NullPointerException: Cannot invoke "Object.hashCode()" because "key" is null
    	at com.oath.common.JwtTokenProvider.isBlacklisted(JwtTokenProvider.java:100)
    	at com.oath.common.JwtTokenProvider.validateToken(JwtTokenProvider.java:64)
    	at com.oath.common.auth.AuthInterceptor.preHandle(AuthInterceptor.java:53)
    ```
*   **분석:**
    *   `GET /api/groups/{groupId}/metrics/summary` 엔드포인트 호출 시, `AuthInterceptor`가 `Authorization` 헤더를 전혀 받지 못하고 있습니다 (`null`).
    *   이로 인해 `JwtTokenProvider.validateToken` 내부에서 `null` 토큰을 캐시 조회에 사용하려다 `NullPointerException`이 발생했습니다.
    *   이는 **클라이언트(Postman, Swagger UI, Flutter 앱 등)가 해당 API 호출 시 `Authorization: Bearer <TOKEN>` 헤더를 제대로 전송하지 않고 있다**는 명확한 증거입니다.
    *   사용자께서는 환경변수를 통해 토큰을 관리하며 다른 API는 정상 작동한다고 하셨으나, 이 특정 API 호출 시에만 헤더가 누락되고 있습니다.

### 2.2. 🐍 Python AI 서버 로그의 404 Not Found

*   **로그:**
    ```
    INFO:     192.168.0.187:59218 - "POST / HTTP/1.1" 404 Not Found
    ```
*   **분석:**
    *   Spring 서버가 Python AI 서버의 **루트 경로 (`/`)**로 `POST` 요청을 보내고 있음을 나타냅니다.
    *   올바른 엔드포인트는 `/metrics/group/summary`입니다.
    *   이는 `MetricsGroupService`에서 AI 서버로 요청을 보내는 URI 구성에 문제가 있거나, `ai.server.url` 값이 올바르게 주입되지 않았을 가능성을 시사합니다.
    *   **이 문제는 401 Unauthorized 에러가 해결되어 `MetricsGroupService`가 실제로 호출된 이후에 발생할 문제입니다.** 현재는 인증 문제에 가려져 있습니다.

---

## 3. 현재까지 진행된 작업 요약 (이전 작업)

1.  **그룹 요약 기능 구현:**
    *   `Group` 엔티티에 `summary`, `summaryStatus`, `summaryLastUpdatedAt` 필드 및 `updateSummary` 메서드 추가.
    *   `SummaryStatus` Enum 클래스 생성.
    *   `PlanJpaRepository`에 `findCompletedPlanIdsByGroupId` 메서드 추가.
    *   `MetricsGroupService` 생성 및 AI 서버 연동 로직 구현 (초기에는 `WebClient`, 이후 `java.net.http.HttpClient`로 변경).
    *   `GroupController`에 `GET /api/groups/{groupId}/metrics/summary` 엔드포인트 추가.
    *   AI 서버 통신용 DTO (`AiGroupSummaryRequest`, `AiGroupSummaryResponse`, `AiGroupSummaryStats`) 생성.
2.  **환경 설정 및 디버깅:**
    *   `build.gradle`에서 `spring-boot-starter-webflux`를 `spring-webflux`로 변경.
    *   `application-local.yml`에서 `spring.main.web-application-type: servlet` 설정 제거.
    *   `application-local.yml`의 `ai.server.url`을 `http://192.168.0.3:8001`로 수정.
    *   `application-local.yml`에 `logging.level.com.oath: debug` 추가.
    *   `AuthInterceptor` 및 `MetricsGroupService`에 상세 디버그 로그 추가.
    *   `JwtTokenProvider.validateToken`에 `null` 토큰 방어 로직 추가 (NPE 방지).
3.  **샘플 데이터 초기화:**
    *   `DataInitializer12_Plan`에서 샘플 그룹에 대해 `MetricsGroupService.requestGroupSummary`를 호출하도록 추가.

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

---

## 5. 앞으로의 작업 (To-Do)

`@/docs/choong/1125_todo.md` 문서의 내용을 기반으로 현재 문제 해결에 집중합니다.

### 5.1. 🚨 최우선 해결 과제: 401 Unauthorized 에러 해결

*   **목표:** `GET /api/groups/{groupId}/metrics/summary` API 호출 시 `Authorization` 헤더가 Spring 서버에 정상적으로 도달하도록 합니다.
*   **세부 계획:**
    1.  **클라이언트가 실제로 전송하는 HTTP 요청 헤더 확인:**
        *   사용하시는 클라이언트 도구(Postman, Swagger UI, cURL 명령어, Flutter 코드 등)에서 `GET /api/groups/{groupId}/metrics/summary` 호출 시 **실제로 전송되는 HTTP 요청 헤더 전체**를 캡처하여 공유해주십시오.
        *   특히 Swagger UI를 사용하신다면, 브라우저 개발자 도구(F12)의 Network 탭에서 해당 요청의 Request Headers를 확인하는 것이 가장 정확합니다.
    2.  **유효한 JWT 토큰 사용 확인:**
        *   `Authorization: Bearer <YOUR_VALID_JWT_TOKEN>` 형식으로 올바르게 토큰이 포함되었는지 다시 한번 확인합니다. 토큰 자체의 유효기간이 만료되었을 수도 있습니다.

### 5.2. 🐍 다음 해결 과제: Python AI 서버 404 Not Found 해결

*   **목표:** Spring 서버가 Python AI 서버의 올바른 엔드포인트(`/metrics/group/summary`)로 요청을 보내도록 수정합니다.
*   **세부 계획 (5.1 해결 후 진행):**
    1.  `MetricsGroupService`의 디버그 로그(`aiServerBaseUrl`, `Final AI Summary Request URI`)를 통해 Spring 서버가 실제로 어떤 URL로 요청을 보내려 했는지 확인합니다.
    2.  필요하다면 `ai.server.url` 설정 또는 `MetricsGroupService` 내 URI 구성 로직을 수정합니다.

---

## 6. 관련 문서

*   **작업 규칙:**
    *   `@/docs/규칙_코딩컨벤션_by김근호.md`
    *   `@/docs/규칙_서비스_트랜잭션.md`
*   **AI 요약 기능 관련:**
    *   `@/docs/choong/1125d_재작업_가이드.md` (재작업 가이드)
    *   `@/docs/choong/1125c_파이썬API연동_작업기록.md` (파이썬 API 연동 작업 기록)
    *   `@/docs/choong/1125f_파이썬API스펙문서.md` (파이썬 AI 서버 API v2 구현 완료 회신)
    *   `@/docs/PYTHON_API_REQ.md` (파이썬 AI 서버 API 최종 명세서)
    *   `@/docs/METRIC_API.md` (Metrics & AI 요약 API 명세 - 클라이언트용)
    *   `@/docs/GROUP_API.md` (Group API 명세서 - 클라이언트용)
*   **현재 문제 분석:**
    *   `@/docs/choong/1125g_그룹AI요청실패원인분석.md` (그룹 AI 요청 실패 원인 분석)
*   **오늘의 할 일:**
    *   `@/docs/choong/1125_todo.md`
