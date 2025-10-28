# ✅ 플랜 서비스 인가(Authorization) 완료

## 📌 작업 완료 내용

### 🔐 권한 검증 메서드 추가 (PlanService.java)

```java
// 1. 플랜 접근 권한 검증 (생성자 또는 참가자만)
public void validatePlanAccess(Long planId, Long memberId)

// 2. 플랜 생성자 권한 검증 (생성자만)
public void validatePlanCreator(Long planId, Long memberId)
```

---

## 🎯 API별 권한 검증 적용 현황

| API | 인증 | 권한 검증 | 설명 |
|-----|------|----------|------|
| `GET /plans` | ✅ @Auth | 본인 플랜만 조회 | 생성/참여한 플랜만 |
| `GET /plans/{id}` | ✅ @Auth | ✅ validatePlanAccess | 생성자 또는 참가자 |
| `POST /plans` | ✅ @Auth | - | 로그인한 사용자 |
| `PUT /plans/{id}` | ✅ @Auth | ✅ validatePlanCreator | 생성자만 |
| `DELETE /plans/{id}` | ✅ @Auth | ✅ validatePlanCreator | 생성자만 |
| `POST /plans/{id}/participants` | ✅ @Auth | ✅ Service 내부 검증 | 생성자만 |
| `DELETE /participants/{id}` | ✅ @Auth | ✅ 생성자 또는 본인 | 탈퇴 가능 |
| `PUT /participants/{id}/status` | ✅ @Auth | ✅ Service 내부 검증 | 본인만 |
| `GET /plans/{id}/participants` | ✅ @Auth | ✅ validatePlanAccess | 생성자 또는 참가자 |
| `POST /participants/{id}/departure` | ✅ @Auth | ✅ 본인 확인 | 본인만 |
| `POST /participants/{id}/arrival` | ✅ @Auth | ✅ 본인 확인 | 본인만 |
| `POST /participants/{id}/suggest-departure` | ✅ @Auth | ✅ 본인 확인 | 본인만 |
| `GET /participants/{id}/late-fine` | ✅ @Auth | ✅ 본인 또는 생성자 | 민감 정보 |
| `POST /plans/{id}/confirm-place` | ✅ @Auth | ✅ validatePlanCreator | 생성자만 |

---

## 🔒 권한 검증 로직 상세

### 1️⃣ 플랜 접근 권한 (생성자 또는 참가자)

```java
public void validatePlanAccess(Long planId, Long memberId) {
    Plan plan = getPlanById(planId);
    boolean isCreator = plan.getCreatorMember().getId().equals(memberId);
    boolean isParticipant = plan.getParticipants().stream()
            .anyMatch(p -> p.getMember().getId().equals(memberId));

    if (!isCreator && !isParticipant) {
        throw new Exception403("이 플랜에 접근할 권한이 없습니다.");
    }
}
```

**적용 API**:
- 플랜 조회 (`GET /plans/{id}`)
- 참가자 목록 조회 (`GET /plans/{id}/participants`)

---

### 2️⃣ 플랜 생성자 권한 (생성자만)

```java
public void validatePlanCreator(Long planId, Long memberId) {
    Plan plan = getPlanById(planId);
    if (!plan.getCreatorMember().getId().equals(memberId)) {
        throw new Exception403("플랜 생성자만 수정/삭제할 수 있습니다.");
    }
}
```

**적용 API**:
- 플랜 수정 (`PUT /plans/{id}`)
- 플랜 삭제 (`DELETE /plans/{id}`)
- 장소 확정 (`POST /plans/{id}/confirm-place`)

---

### 3️⃣ 참가자 추가 권한 (생성자만)

```java
public Participant addParticipant(Long planId, Long memberId, Long requesterId) {
    Plan plan = getPlanById(planId);
    
    // 권한 검증: 플랜 생성자만 참가자 추가 가능
    if (!plan.getCreatorMember().getId().equals(requesterId)) {
        throw new Exception403("플랜 생성자만 참가자를 추가할 수 있습니다.");
    }
    // ...
}
```

---

### 4️⃣ 참가자 삭제 권한 (생성자 또는 본인)

```java
public void removeParticipant(Long participantId, Long requesterId) {
    Participant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new Exception404("해당 참가자를 찾을 수 없습니다."));
    
    Plan plan = participant.getPlan();
    boolean isCreator = plan.getCreatorMember().getId().equals(requesterId);
    boolean isSelf = participant.getMember().getId().equals(requesterId);
    
    if (!isCreator && !isSelf) {
        throw new Exception403("플랜 생성자 또는 본인만 참가자를 삭제할 수 있습니다.");
    }
    
    participantRepository.deleteById(participantId);
}
```

**기능**: 
- 생성자: 모든 참가자 강퇴 가능
- 참가자: 본인만 탈퇴 가능

---

### 5️⃣ 참가자 상태 변경 권한 (본인만)

```java
public Participant changeParticipantStatus(Long participantId, ParticipantStatus status, Long requesterId) {
    Participant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
    
    // 권한 검증: 참가자 본인만 상태 변경 가능
    if (!participant.getMember().getId().equals(requesterId)) {
        throw new Exception403("본인의 참가 상태만 변경할 수 있습니다.");
    }
    
    participant.setParticipantStatus(status);
    return participantRepository.save(participant);
}
```

---

### 6️⃣ 출발/도착 기록 권한 (본인만)

```java
public Participant recordDeparture(Long participantId, LocalDateTime actualDeparture, Long requesterId) {
    Participant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

    // 본인 확인
    if (!participant.getMember().getId().equals(requesterId)) {
        throw new Exception403("본인의 출발 시간만 기록할 수 있습니다.");
    }
    // ...
}

public Participant recordArrival(Long participantId, LocalDateTime actualArrival, Long requesterId) {
    Participant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

    // 본인 확인
    if (!participant.getMember().getId().equals(requesterId)) {
        throw new Exception403("본인의 도착 시간만 기록할 수 있습니다.");
    }
    // ...
}
```

---

### 7️⃣ 지각 벌금 조회 권한 (본인 또는 플랜 생성자)

```java
public Long calculateLateFine(Long participantId, Long requesterId) {
    Participant pm = participantRepository.findById(participantId)
            .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
    
    // 권한 검증: 본인 또는 플랜 생성자만 조회 가능
    boolean isOwner = pm.getMember().getId().equals(requesterId);
    boolean isCreator = pm.getPlan().getCreatorMember().getId().equals(requesterId);
    
    if (!isOwner && !isCreator) {
        throw new Exception403("본인의 벌금만 조회할 수 있습니다.");
    }
    // ...
}
```

**이유**: 벌금은 민감한 정보이므로 타인의 벌금은 조회 불가

---

## 🔄 인증/인가 흐름

```
1. 클라이언트 요청 (JWT 토큰 포함)
         ↓
2. AuthInterceptor
   - @Auth 확인
   - JWT 토큰 검증
   - request.setAttribute("userEmail", email)
         ↓
3. Controller
   - getCurrentMember(request) → DB 조회
   - 권한 검증 메서드 호출
         ↓
4. Service
   - 비즈니스 로직 실행
   - 추가 권한 검증 (필요 시)
         ↓
5. 응답 반환
```

---

## 📊 예외 처리

| 예외 | HTTP Status | 발생 상황 |
|------|-------------|----------|
| `Exception401` | 401 Unauthorized | JWT 토큰 없음/만료/잘못됨 |
| `Exception403` | 403 Forbidden | 권한 없음 (생성자 아님, 참가자 아님) |
| `Exception404` | 404 Not Found | 플랜/참가자를 찾을 수 없음 |

---

## 🧪 테스트 시나리오

### ✅ 정상 케이스

```bash
# 1. 로그인 (JWT 토큰 획득)
POST /api/auth/login
→ JWT 토큰 반환

# 2. 내 플랜 조회 (성공)
GET /api/plans
Authorization: Bearer {JWT_TOKEN}
→ 본인이 생성/참여한 플랜만 반환

# 3. 플랜 수정 (생성자만 성공)
PUT /api/plans/1
Authorization: Bearer {JWT_TOKEN}
→ 생성자면 200 OK
```

### ❌ 권한 없는 케이스

```bash
# 1. 다른 사람 플랜 조회 시도
GET /api/plans/999
Authorization: Bearer {JWT_TOKEN}
→ 403 Forbidden: "이 플랜에 접근할 권한이 없습니다."

# 2. 참가자가 플랜 수정 시도
PUT /api/plans/1
Authorization: Bearer {JWT_TOKEN (참가자)}
→ 403 Forbidden: "플랜 생성자만 수정/삭제할 수 있습니다."

# 3. 다른 사람의 출발 시간 기록 시도
POST /api/plans/participants/5/departure
Authorization: Bearer {JWT_TOKEN}
→ 403 Forbidden: "본인의 출발 시간만 기록할 수 있습니다."
```

---

## 📝 변경된 파일 목록

### Service
- ✏️ `PlanService.java`
  - `validatePlanAccess()` 추가
  - `validatePlanCreator()` 추가
  - `removeParticipant()` 권한 검증 추가
  - `recordDeparture()` 본인 확인 추가
  - `recordArrival()` 본인 확인 추가
  - `suggestExpectedDeparture()` 본인 확인 추가

### Facade
- ✏️ `ParticipantFacade.java`
  - `recordDeparture()` requesterId 파라미터 추가
  - `recordArrival()` requesterId 파라미터 추가
  - `suggestExpectedDeparture()` requesterId 파라미터 추가

### Controller
- ✏️ `PlanRestController.java`
  - 모든 API에 권한 검증 추가
  - `getCurrentMember()` 헬퍼 메서드 활용

---

## 🚀 성능 최적화 완료

### ✅ JWT에 memberId 추가 (완료)

**이전**: 매 요청마다 이메일로 Member DB 조회
```java
Member getCurrentMember(HttpServletRequest request) {
    String email = (String) request.getAttribute("userEmail");
    return memberRepository.findByEmail(email).orElseThrow(...); // ❌ DB 조회
}
```

**현재**: JWT에서 memberId를 직접 추출하여 PK로 조회
```java
// JwtTokenProvider.java
public String createToken(String email, Role role, Long memberId) {
    return Jwts.builder()
            .subject(email)
            .claim("role", role.name())
            .claim("memberId", memberId) // ✅ 추가됨
            .build();
}

public Long getMemberId(String token) {
    return parseClaims(token).get("memberId", Long.class);
}

// AuthInterceptor.java
Long memberId = jwtTokenProvider.getMemberId(token);
request.setAttribute("memberId", memberId); // ✅ request에 저장

// Controller에서
Member getCurrentMember(HttpServletRequest request) {
    Long memberId = (Long) request.getAttribute("memberId");
    return memberRepository.findById(memberId).orElseThrow(...); // ✅ PK 조회
}
```

**효과**:
- ✅ 이메일 인덱스 조회 → PK 조회로 변경 (더 빠름)
- ✅ 매 요청마다 DB 조회는 여전히 발생하지만 성능 향상
- ✅ JWT 토큰 크기 증가: 약 8-10 바이트 (Long 타입)

**적용된 로그인 방식**:
- ✅ 이메일 로그인 (`POST /api/member/login`)
- ✅ Kakao 소셜 로그인 (`POST /api/member/kakao/doLogin`)
- ✅ Facebook 소셜 로그인 (`POST /api/member/facebook/doLogin`)

---

## 🚀 향후 개선 사항

### 2. @AuthUser 파라미터 리졸버 (코드 간결화)

```java
// 현재
@GetMapping("/plans")
public ResponseEntity<?> listPlans(HttpServletRequest request) {
    Member currentMember = getCurrentMember(request); // 중복 코드
    // ...
}

// 개선안
@GetMapping("/plans")
public ResponseEntity<?> listPlans(@AuthUser Member currentMember) {
    // 바로 사용 가능!
}
```

---

## ✅ 완료 체크리스트

- [x] JWT 인증 구현
- [x] 플랜 접근 권한 검증
- [x] 플랜 생성자 권한 검증
- [x] 참가자 추가 권한 검증
- [x] 참가자 삭제 권한 검증 (생성자 또는 본인)
- [x] 참가자 상태 변경 권한 검증 (본인)
- [x] 출발/도착 기록 권한 검증 (본인)
- [x] 지각 벌금 조회 권한 검증 (본인 또는 생성자)
- [x] 장소 확정 권한 검증 (생성자)
- [x] 예외 처리 (401, 403, 404)
- [x] JWT에 memberId 추가 ✅ **완료!**
- [ ] @AuthUser 리졸버 구현 (TODO)

---

**작업 완료일**: 2025-10-28  
**작업자**: GitHub Copilot  
**소요 시간**: 약 30분

🎉 **플랜 서비스의 모든 API에 인증/인가 처리가 완료되었습니다!**

