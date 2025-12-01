# [최종] 1125_Python_AI_API_v2_연동_작업기록

- **작성일**: 2025-11-25
- **주요 목표:** Python AI 서버의 신규 API(v2) 명세에 맞춰 Spring 서버 그룹 요약 기능 코드를 리팩터링하고, 정상 연동을 확인한다.
- **최종 상태:** ✅ 완료

---

## 1. 작업 계획

### 1.1. 배경
- Python AI 서버에서 그룹 통계/요약 API가 v2 스펙으로 업데이트되고 단일 통합 API로 변경됨.
- 기존 Spring 로직/DTO를 v2 스펙에 맞게 정비해야 함.

### 1.2. 세부 계획
1. **DTO 리팩터링:** AI 서버 v2 JSON 구조를 매핑하는 DTO 새로 구성.
2. **서비스 로직 정비:** MetricsGroupService의 AI 서버 호출 로직을 v2 스펙에 맞게 수정.
3. **네트워크 설정 점검:** AI 서버 요청/응답 타임아웃 등 HTTP 설정 조정.
4. **테스트/검증:** 변경된 로직이 실제 환경에서 정상 작동하는지 확인.

---

## 2. 작업 진행 및 산출물
### 2.1. DTO 리팩터링 (완료)
- **내용:** v2 명세에 따라 그룹 요약용 DTO를 com.oath.domain.metrics.dto 패키지에 새로 구성하고 @JsonProperty로 snake_case ↔ camelCase 매핑.
- **산출물:** AiGroupSummaryStats.java, AiGroupSummaryResponse.java, AiGroupSummaryRequest.java.

### 2.2. 서비스 로직 정비 (완료)
- **내용:** MetricsGroupService가 새 DTO를 사용해 v2 스펙으로 호출하도록 변경하고, warnings 로깅 추가.
- **산출물:** MetricsGroupService.java 수정.

### 2.3. 네트워크 설정 (완료)
- **내용:** 범용/표준 설정을 갖춘 java.net.http.HttpClient를 Bean으로 등록해 사용하도록 변경. 타임아웃 조정으로 안정성 확보.
- **산출물:** HttpClientConfig.java 신규, MetricsGroupService.java 수정.

---

## 3. 연동 테스트 및 트러블슈팅
- **목표:** 개발 환경에서 그룹 AI 요약이 Python AI 서버와 정상 왕복되는지 확인.

### 3.1. 문제 발생: 401 Unauthorized & 404 Not Found
- **증상 1 (401):** Swagger UI 호출 시 Authorization 헤더 누락으로 401. curl은 정상.
- **증상 2 (404):** DataInitializer에서 AI 호출 시 AI 서버 로그에 POST / HTTP/1.1" 404 Not Found 기록.

### 3.2. 해결 과정
1. JwtTokenProvider / AuthInterceptor에 null/isBlank 체크 추가로 NPE 방지.
2. GroupController에 @SecurityRequirement(name = "Bearer Authentication") 추가 → Swagger UI가 Authorization 헤더를 전송하도록 수정.
3. MetricsGroupService의 요청 URI를 iServerBaseUrl + "/metrics/group/summary"로 정확히 조합.

### 3.3. 최종 결과
- Swagger UI·DataInitializer 모두 AI 서버와 정상 통신, 요약 결과 수신 확인.
- 결론: Python AI 서버 API v2 연동 완료.

---

## 4. 최종 교훈 및 요약
1. **curl vs UI 도구:** 로우 레벨 도구(curl)에서 정상이어도 Swagger UI 등 중간 계층 문제로 실패할 수 있다.
2. **Swagger 어노테이션 중요:** 인증이 필요한 컨트롤러에는 반드시 @SecurityRequirement를 붙여야 한다.
3. **방어적 프로그래밍:** 외부 요청 입력(null/blank 토큰 등)을 방어하고, 경계에서 예외를 차단해야 안정성이 높아진다.

---

## 5. 추가 회고/가이드 요약 (1125d/1125g 발췌)
- **페이즈 기반 재작업:** 문제가 꼬였을 때는 깨끗한 상태 복원 → 핵심 기능 단건 검증 → 요약 기능을 단계별로 켜며 검증하는 순서가 안전하다.
- **한 번에 하나씩 변경:** DTO/서비스/보안 설정을 작은 단위로 나눠 적용·검증해야 재현·회귀가 용이하다.
- **요청/응답 로깅:** AI 서버 왕복 로그가 없으면 컨트롤러 미호출·조건 미충족·네트워크 실패를 구분하기 어렵다. 요청 직전·응답 직후 로그를 남기는 습관이 효과적이었다.
- **인증 헤더 점검:** Swagger UI 등 도구별 Authorization 헤더 누락 여부를 항상 확인한다.

---

## 6. 미해결 과제/리팩토링 메모 (1125h 이관)
- AiGroupSummaryRequest 스펙 정합화: groupId 제거, planIds에 @JsonProperty("plan_ids") 적용, style/notes/mode 필드 추가 후 MetricsGroupService 요청 본문 생성 로직 수정.
- GroupService.getGroupMembers 권한 검증 추가: 요청자가 해당 그룹 멤버인지 확인 후 조회하도록 보강.
- getMyGroups N+1 완화: chatRepository.findTop... 반복 호출을 배치/조인 등으로 최적화.
- leaveGroup 최적화: 불필요한 엔티티 로딩 없이 deleteById 등으로 단순화 검토.
