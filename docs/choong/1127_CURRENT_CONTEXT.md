# Current Problem Context (2025-11-28 기준)

- **문서 목적:** 본 문서는 새 작업자가 현재 프로젝트의 맥락을 빠르게 파악하고, **즉시 다음 작업을 이어받아 수행할 수 있도록** 현재 직면한 문제와 다음 목표에 대한 핵심 정보를 제공합니다.

---

## 0. 작업 규칙 및 코드 규칙

본 작업은 다음 규칙들을 준수합니다.

1.  **한글로만 소통:** 모든 의사소통은 한글로 진행합니다.
2.  **거절하면 즉시 작업 중지:** 요청에 대한 거절 의사가 있다면 즉시 작업을 중지합니다.
3.  **`@/docs/규칙_코딩컨벤션_by김근호.md` 준수:** 코딩 스타일 및 컨벤션을 따릅니다.
4.  **`@/docs/규칙_서비스_트랜잭션.md` 준수:** 서비스 트랜잭션 관련 규칙을 따릅니다.

---

## 1. 현재 목표

### 1.1. AI 요약 기능 클라이언트 연동 지원 (우선순위 1)

- **현재 상태:** 서버 측 AI 요약 기능 개발 완료 ✅
  - 플랜 요약: 개별 약속 완료 시 자동 생성 및 저장
  - 그룹 요약: 플랜 처리 후 그룹 통계 기반 자동 생성 및 저장
  - 이벤트 체이닝 아키텍처로 안정성 확보
  - **테스트 데이터 준비 완료:** `PlanScenario01_Completed`에 Group 통계 및 AI 요약 포함
  - **PR 리뷰 반영 완료 (2025-11-28):** JPA 변경 감지 메커니즘 활용, 불필요한 `save()` 호출 제거
- **클라이언트 지원 문서:**
  - `1127a_클라이언트연동지원계획.md` - 연동 지원 전체 계획
  - `1127e_약속요약기능_QNA.md` - AI 요약 기능 Q&A
- **다음 작업:**
  1. 클라이언트팀과 소통하여 UI 구현 현황 파악
  2. API 연동 중 발생하는 질문/이슈에 즉시 대응
  3. 필요 시 추가 테스트 데이터 또는 API 제공

### 1.2. 실시간 지도 기능 클라이언트 연동 (우선순위 2)

- **현재 상태:** 서버 측 WebSocket 및 관련 API 준비 완료 ✅
  - 테스트용 API 구현 완료: `/api/plans/{planId}/participants/{participantId}/test/force-movement-status`
  - 클라이언트 전달용 문서: `LIVE_MAP_API.md`
  - **클라이언트 스펙 확인 완료:** `1127c_실시간지도_QNA.md`
- **다음 작업:**
  1. 클라이언트 연동 현황 파악 (`LIVE_MAP_API.md` 기반 구현 진행 상황)
  2. WebSocket 연결 테스트 지원
  3. 테스트 API 활용 가이드 제공

---

## 2. 핵심 경로

### 2.1. AI 요약 기능 관련 파일

**엔티티/Enum:**

- `C:/workspace/oath/src/main/java/com/oath/domain/groups/Group.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/groups/SummaryStatus.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/plan/Plan.java`

**리포지토리:**

- `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupRepository/GroupRepository.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/plan/repository/PlanJpaRepository.java`

**서비스:**

- `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupService/GroupService.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupService/MetricsGroupService.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/metrics/MetricsPushService.java`

**컨트롤러:**

- `C:/workspace/oath/src/main/java/com/oath/domain/groups/groupController/GroupController.java`

**DTO (AI 통신용):**

- `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiGroupSummaryRequest.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiGroupSummaryResponse.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiPlanSummaryRequest.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/metrics/dto/AiPlanSummaryResponse.java`

**이벤트 리스너:**

- `C:/workspace/oath/src/main/java/com/oath/domain/plan/event/listener/PlanCompletionListener.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/groups/event/listener/GroupUpdateListener.java`

**초기화 데이터:**

- `C:/workspace/oath/src/main/java/com/oath/initializer/scenario/PlanScenario01_Completed.java`
- `C:/workspace/oath/src/main/java/com/oath/initializer/scenario/PlanScenario02_Ongoing.java`
- `C:/workspace/oath/src/main/java/com/oath/initializer/scenario/PlanScenario03_Pending.java`

### 2.2. 실시간 지도 기능 관련 파일

- `C:/workspace/oath/src/main/java/com/oath/domain/plan/controller/PlanTestApiController.java` (테스트 API)
- `C:/workspace/oath/src/main/java/com/oath/common/config/WebSocketConfig.java`
- `C:/workspace/oath/src/main/java/com/oath/domain/plan/event/listener/ArrivalListener.java`
- `C:/workspace/oath/docs/LIVE_MAP_API.md` (클라이언트 전달용 API 명세)

### 2.3. 핵심 공용 파일

**인증/보안:**

- `C:/workspace/oath/src/main/java/com/oath/common/config/WebMvcConfig.java`
- `C:/workspace/oath/src/main/java/com/oath/common/auth/AuthInterceptor.java`
- `C:/workspace/oath/src/main/java/com/oath/common/JwtTokenProvider.java`

**HTTP 클라이언트:**

- `C:/workspace/oath/src/main/java/com/oath/common/config/WebClientConfig.java` (WebClient 설정 파일, 현재는 `java.net.http.HttpClient` 사용)

**설정:**

- `C:/workspace/oath/src/main/resources/application-local.yml`
- `C:/workspace/oath/build.gradle`

### 2.4. 문서 및 가이드

**API 명세 (클라이언트 연동용):**

- `docs/GROUP_API.md` (2025-11-26 업데이트) - 그룹/채팅 API, AI 요약 포함
- `docs/METRIC_API.md` (2025-11-26 업데이트) - AI 요약 폴링 가이드
- `docs/LIVE_MAP_API.md` (2025-11-26 업데이트) - 실시간 지도 WebSocket
- `docs/PLAN_API.md` - 약속 관리 핵심 API
- `docs/MEMBER_API.md` - 회원 인증/관리
- `docs/ERROR(에러응답)API.md` - 공통 에러 응답 형식

**Python AI 서버 연동:**

- `docs/1125_ 파이썬API스펙문서.md` (2025-11-26 업데이트) - Python AI 서버 API v2 명세

**트러블슈팅 & 참고자료 (ERROR 태그):**

- `docs/choong/ERROR_1024_카카오로그인.md` - 카카오 소셜 로그인 완벽 가이드
- `docs/choong/ERROR_1117_AI서버연결실패.md` - 하드코딩 IP 오류 교훈
- `docs/choong/ERROR_1118_커넥션폭풍.md` - 멀티 DB 트랜잭션 아키텍처 (ADR)
- `docs/choong/ERROR_1125_스웨거UI401에러.md` - Swagger UI 인증 문제

**코딩 규칙:**

- `docs/규칙_코딩컨벤션_by김근호.md`
- `docs/규칙_서비스_트랜잭션.md`
