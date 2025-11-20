# 약속(Plan), 후기(Review), 댓글(Reply) API 명세 (Flutter용)

간결하게 필요한 엔드포인트, 요청/응답 형태, 권한, 주의사항만 정리했습니다.

## 공통
- Base URL: /api
- 인증: `@Auth`가 필요한 엔드포인트는 JWT 또는 미들웨어로 `memberId`를 request에 세팅합니다.
- 날짜/시간: ISO-8601 (예: 2025-11-20T15:30:00)

---

## 약속(Plan)

### GET /plans/{planId}
- 설명: 플랜 상세 조회
- 응답: Plan 객체

### POST /plans
- 설명: 플랜 생성
- 바디: { title, planDatetime, status, lateFineAmount }
- 응답: 201 Created + Plan

### POST /plans/{planId}/confirm
- 설명: 장소 확정 또는 플랜 확정 (컨트롤러에서 `confirmFinalPlan` 호출)
- 권한: 생성자

### POST /participants/{participantId}/arrival
- 설명: 도착 기록
- 권한: 참가자 본인
- 바디: { actualArrival?: ISOString }
- 동작: 도착 기록 -> ArrivalEvent 발행 -> (모든 ACCEPTED 참가자 도착시) 플랜 자동완료

### POST /plans/{planId}/complete
- 설명: 플랜 수동 완료
- 권한: 생성자

---

## 후기(Review)

### POST /api/reviews
- 설명: 후기 작성 (약속이 완료된 경우에만 가능)
- 권한: 인증 필요
- 바디: { planId, title, content }
- 오류: 약속 미완료이면 400 반환
- 응답: 201 Created + ReviewDTO

### GET /api/reviews/{reviewId}
- 설명: 후기 단건 조회
- 응답: ReviewDTO (replies 포함)

### GET /api/reviews/plan/{planId}
- 설명: 해당 약속의 후기 목록
- 응답: List<ReviewDTO>

### GET /api/reviews/my
- 설명: 내가 작성한 후기 (페이징)
- 권한: 인증 필요

### PUT /api/reviews/{reviewId}
- 설명: 후기 수정 (작성자만)
- 바디: { title?, content? }

### DELETE /api/reviews/{reviewId}
- 설명: 후기 삭제 (작성자만)

---

## 댓글(Reply)

### POST /api/replies/review/{reviewId}
- 설명: 후기 댓글 작성
- 권한: 인증 필요
- 바디: { content }
- 응답: 201 Created + ReplyDTO

### PUT /api/replies/{replyId}
- 설명: 댓글 수정 (작성자만)
- 권한: 인증 필요
- 바디: { content }

### DELETE /api/replies/{replyId}
- 설명: 댓글 삭제 (작성자만)

---

## Completed Plans / 후기 작성 가능 목록 (추가)

### GET /api/plans/my?status=COMPLETED
- 설명: 로그인한 사용자가 생성하거나 참여한 `COMPLETED` 상태의 약속 목록 조회
- 권한: 인증 필요 (`@Auth`)
- 쿼리파라미터:
  - `page`, `size` (선택)
- 응답 항목(PlanDTO 배열):
  - id, title, planDatetime, status, completedAt, placeName, isCreator(boolean)
- 사용처: 후기 탭에서 "종료된 약속" 리스트를 보여줄 때 사용

### GET /api/reviews/eligible
- 설명: 로그인한 사용자가 후기 작성 가능한 약속(완료되었고 아직 후기를 작성하지 않은 약속) 목록 조회
- 권한: 인증 필요
- 응답 항목: Plan 간단 정보 배열
- 비즈니스 룰: 서버에서 `plan.isCompleted() && !reviewExists(planId, memberId)`로 판정

---

## 주의사항 & 비즈니스 룰 (업데이트)
- 후기 작성 가능 조건: `Plan.isCompleted() == true`.
- 프론트엔드 권장 플로우:
  1) 후기 탭 진입 시 `GET /api/reviews/eligible` 호출해 작성 가능한 약속만 노출.
  2) 전체 종료된 약속 목록을 보려면 `GET /api/plans/my?status=COMPLETED` 호출.
- 에러/예외: 서버에서 LazyInitializationException 등으로 응답 실패 시 500을 받을 수 있으니, 클라이언트는 재요청 또는 에러 메시지 표시 로직을 구현하세요.

---

## 예시: 후기 작성 요청 (Flutter)
- URL: POST /api/reviews
- 헤더: Authorization: Bearer {token}
- 바디:
  {
    "planId": 123,
    "title": "즐거운 모임",
    "content": "오늘 모임 정말 좋았어요!"
  }

- 가능한 응답 코드
  - 201: 성공
  - 400: 약속 미완료 또는 이미 후기 작성됨
  - 401: 인증 필요
  - 404: 멤버 또는 플랜 없음
