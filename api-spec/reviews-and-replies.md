# 후기(Review) 및 댓글(Reply) API 명세

## 공통
- Base URL: /api/v1
- 인증: Bearer JWT (헤더 `Authorization: Bearer <token>`)
- Content-Type: application/json (파일 업로드는 multipart/form-data 별도)
- 시간포맷: ISO 8601 (예: 2025-11-13T12:34:56Z)
- 페이지네이션: query `page`(0 기반, 기본 0), `size`(기본 20), `sort`
- 에러: 400(검증), 401(인증), 403(권한), 404(리소스 없음), 500(서버)

## 후기(Review)
- GET /reviews
  - 설명: 후기 목록 조회. 필터(postId), 페이지네이션, 정렬 지원
  - Query: postId(optional), page, size, sort
  - 반환 항목(리스트의 각 요소): id, postId, authorId, content, rating, images[], replyCount, createdAt, updatedAt

- GET /reviews/{reviewId}
  - 설명: 후기 상세 조회
  - 반환 항목: id, postId, authorId, content, rating, images[], createdAt, updatedAt, replies[]
  - replies 요소: id, authorId, content, createdAt, updatedAt

- POST /reviews
  - 설명: 후기 생성 (인증 필요)
  - 요청 필드: postId (required), content (required, max 2000), rating (optional, 1~5), images (optional - 별도 업로드 가능)
  - 권한/검증: postId 존재, content 비어있지 않음
  - 응답: 201 Created (새 리소스 id 반환)

- PUT /reviews/{reviewId}
  - 설명: 후기 수정 (작성자만)
  - 요청 필드: content, rating, images(선택)
  - 응답: 200 OK (수정된 리소스 또는 확인 메시지)

- DELETE /reviews/{reviewId}
  - 설명: 후기 삭제 (작성자 또는 관리자)
  - 응답: 204 No Content

## 댓글(Reply)
- GET /reviews/{reviewId}/replies
  - 설명: 특정 후기의 댓글 목록 조회
  - Query: page, size, sort
  - 반환 항목(리스트의 각 요소): id, reviewId, authorId, content, createdAt, updatedAt

- POST /reviews/{reviewId}/replies
  - 설명: 댓글 생성 (인증 필요)
  - 요청 필드: content (required, max 500)
  - 권한/검증: reviewId 존재, content 비어있지 않음
  - 응답: 201 Created (새 리소스 id 반환)

- PUT /reviews/{reviewId}/replies/{replyId}
  - 설명: 댓글 수정 (작성자만)
  - 요청 필드: content (required, max 500)
  - 응답: 200 OK

- DELETE /reviews/{reviewId}/replies/{replyId}
  - 설명: 댓글 삭제 (작성자 또는 관리자)
  - 응답: 204 No Content

## 클라이언트 주의사항
- 모든 필드명은 camelCase 사용
- 페이지네이션은 0 기반
- 오류 응답은 가능하면 필드별 메시지를 포함하여 반환
- 이미지 업로드는 별도 엔드포인트 또는 multipart로 처리 권장

---

원하시면 예시 요청/응답 JSON과 cURL 예시를 추가하거나, Flutter용 간단한 API 호출 샘플(Dart)도 만들어 드립니다.
