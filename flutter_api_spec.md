# Flutter 개발을 위한 API 명세서

**프로젝트**: Oathkeeper  
**Base URL**: `http://your-server.com/api`  
**인증 방식**: JWT Bearer Token  
**작성일**: 2025-01-14

---

## 📋 목차

1. [공통 사항](#공통-사항)
2. [Plan (약속) API](#plan-약속-api)
3. [Review (후기) API](#review-후기-api)
4. [Reply (댓글) API](#reply-댓글-api)

---

## 공통 사항

### 인증 헤더
대부분의 API는 JWT 인증이 필요합니다. (`@Auth` 표시된 API)

```
Authorization: Bearer {JWT_TOKEN}
```

### 공통 응답 형식
모든 응답은 `CommonResponse` 래퍼로 감싸집니다.

```json
{
  "success": true,
  "data": { /* 실제 데이터 */ },
  "message": "성공 메시지"
}
```

**오류 응답**
```json
{
  "success": false,
  "message": "오류 메시지",
  "errorCode": "ERROR_CODE"
}
```

### 날짜/시간 형식
- **ISO 8601 형식**: `2025-01-15T14:30:00`
- 예시: `"2025-01-15T14:30:00"` (LocalDateTime)

### Enum 값

**Status (약속 상태)**
- `PLANNING` - 계획 중
- `CONFIRMED` - 확정됨
- `IN_PROGRESS` - 진행 중
- `COMPLETED` - 완료됨
- `CANCELLED` - 취소됨

**ParticipantStatus (참가자 상태)**
- `PENDING` - 대기 중
- `CONFIRMED` - 참가 확정
- `REJECTED` - 참가 거부
- `DEPARTED` - 출발함
- `ARRIVED` - 도착함

---

## Plan (약속) API

### 1. 약속 목록 조회

**Endpoint**: `GET /api/plans`  
**인증**: 필요  
**설명**: 로그인한 사용자가 생성하거나 참가한 약속 목록 조회

**요청 헤더**
```
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "강남역 모임",
      "date": "2025-01-20",
      "time": "18:00:00",
      "location": "강남역 2번 출구",
      "placeLatitude": 37.497952,
      "placeLongitude": 127.027619,
      "participants": [
        {
          "id": 10,
          "memberId": 5,
          "memberNickname": "홍길동",
          "participantStatus": "CONFIRMED",
          "transportMethod": "SUBWAY",
          "startAddress": "서울 강북구",
          "startLatitude": 37.6397,
          "startLongitude": 127.0252,
          "expectedTravelTimeMinutes": 30,
          "expectedDepartureTime": "2025-01-20T17:30:00",
          "actualDepartureTime": null,
          "actualArrivalTime": null,
          "timeBurdenMinutes": null,
          "departureFailureReason": null
        }
      ]
    }
  ],
  "message": null
}
```

---

### 2. 약속 단건 조회

**Endpoint**: `GET /api/plans/{id}`  
**인증**: 필요  
**설명**: 특정 약속의 상세 정보 조회 (생성자 또는 참가자만 가능)

**경로 파라미터**
- `id` (Long): 약속 ID

**응답**: [약속 목록 조회](#1-약속-목록-조회)와 동일한 `CreatePlan` 객체 (단일)

---

### 3. 추천 약속 목록 조회

**Endpoint**: `GET /api/plans/recommend`  
**인증**: 필요  
**설명**: AI 임베딩 기반 유사 약속 추천

**쿼리 파라미터**
- `currentPlanId` (Long, 필수): 기준이 되는 약속 ID
- `limit` (Long, 필수): 추천 개수 (예: 5)

**예시 요청**
```
GET /api/plans/recommend?currentPlanId=10&limit=5
```

**응답**: [약속 목록 조회](#1-약속-목록-조회)와 동일

---

### 4. 약속 생성

**Endpoint**: `POST /api/plans`  
**인증**: 필요  
**설명**: 새로운 약속 생성

**요청 본문**
```json
{
  "creatorMemberId": 5,
  "title": "홍대 저녁 식사",
  "planDatetime": "2025-01-25T19:00:00",
  "status": "PLANNING",
  "lateFineAmount": 5000
}
```

**필드 설명**
- `creatorMemberId` (Long, 필수): 생성자 회원 ID
- `title` (String, 필수): 약속 제목
- `planDatetime` (String, 필수): 약속 일시 (ISO 8601)
- `status` (String, 선택): 초기 상태 (기본값: `PLANNING`)
- `lateFineAmount` (Long, 선택): 지각 벌금액 (원 단위)

**응답**: [약속 단건 조회](#2-약속-단건-조회)와 동일

---

### 5. 약속 수정

**Endpoint**: `PUT /api/plans/{id}`  
**인증**: 필요  
**설명**: 약속 정보 수정 (생성자만 가능)

**경로 파라미터**
- `id` (Long): 약속 ID

**요청 본문**
```json
{
  "title": "홍대 저녁 식사 (변경)",
  "planDatetime": "2025-01-25T19:30:00",
  "status": "CONFIRMED",
  "tags": ["친구", "저녁", "술"]
}
```

**필드 설명**
- `title` (String, 선택): 수정할 제목
- `planDatetime` (String, 선택): 수정할 일시
- `status` (String, 선택): 수정할 상태
- `tags` (List<String>, 선택): 태그 목록

**응답**: [약속 단건 조회](#2-약속-단건-조회)와 동일

---

### 6. 약속 삭제

**Endpoint**: `DELETE /api/plans/{id}`  
**인증**: 필요  
**설명**: 약속 삭제 (생성자만 가능)

**경로 파라미터**
- `id` (Long): 약속 ID

**응답**
```json
{
  "success": true,
  "data": null,
  "message": "삭제되었습니다."
}
```

---

### 7. 참가자 추가

**Endpoint**: `POST /api/plans/{planId}/participants`  
**인증**: 필요  
**설명**: 약속에 참가자 추가 (생성자만 가능)

**경로 파라미터**
- `planId` (Long): 약속 ID

**요청 본문**
```json
{
  "memberId": 8
}
```

**응답**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "memberId": 8,
    "memberNickname": "김철수",
    "participantStatus": "PENDING",
    "transportMethod": null,
    "startAddress": "서울 마포구",
    "startLatitude": 37.5665,
    "startLongitude": 126.9780,
    "expectedTravelTimeMinutes": null,
    "expectedDepartureTime": null,
    "actualDepartureTime": null,
    "actualArrivalTime": null,
    "timeBurdenMinutes": null,
    "departureFailureReason": null
  },
  "message": null
}
```

---

### 8. 참가자 삭제

**Endpoint**: `DELETE /api/plans/{planId}/participants/{participantId}`  
**인증**: 필요  
**설명**: 참가자 삭제 (생성자 또는 본인만 가능)

**경로 파라미터**
- `planId` (Long): 약속 ID
- `participantId` (Long): 참가자 ID

**응답**
```json
{
  "success": true,
  "data": null,
  "message": "참가자가 삭제되었습니다."
}
```

---

### 9. 참가자 상태 변경

**Endpoint**: `PUT /api/plans/participants/{participantId}/status`  
**인증**: 필요  
**설명**: 참가자 상태 변경 (본인만 가능)

**경로 파라미터**
- `participantId` (Long): 참가자 ID

**요청 본문**
```json
{
  "status": "CONFIRMED"
}
```

**가능한 상태**: `PENDING`, `CONFIRMED`, `REJECTED`

**응답**: [참가자 추가](#7-참가자-추가)와 동일한 `ParticipantResponse`

---

### 10. 참가자 목록 조회

**Endpoint**: `GET /api/plans/{planId}/participants`  
**인증**: 필요  
**설명**: 약속의 참가자 목록 조회

**경로 파라미터**
- `planId` (Long): 약속 ID

**응답**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "memberId": 5,
      "memberNickname": "홍길동",
      "participantStatus": "CONFIRMED",
      "transportMethod": "SUBWAY",
      "startAddress": "서울 강북구",
      "startLatitude": 37.6397,
      "startLongitude": 127.0252,
      "expectedTravelTimeMinutes": 30,
      "expectedDepartureTime": "2025-01-20T17:30:00",
      "actualDepartureTime": null,
      "actualArrivalTime": null,
      "timeBurdenMinutes": null,
      "departureFailureReason": null
    }
  ],
  "message": null
}
```

---

### 11. 출발 기록

**Endpoint**: `POST /api/plans/participants/{participantId}/departure`  
**인증**: 필요  
**설명**: 출발 시간 기록 (본인만 가능)

**경로 파라미터**
- `participantId` (Long): 참가자 ID

**요청 본문**
```json
{
  "time": "2025-01-20T17:35:00"
}
```

**응답**: [참가자 추가](#7-참가자-추가)와 동일 (`actualDepartureTime` 필드 업데이트됨)

---

### 12. 도착 기록

**Endpoint**: `POST /api/plans/participants/{participantId}/arrival`  
**인증**: 필요  
**설명**: 도착 시간 기록 (본인만 가능) - 모든 멤버 도착 시 약속 자동 완료

**경로 파라미터**
- `participantId` (Long): 참가자 ID

**요청 본문**
```json
{
  "time": "2025-01-20T18:05:00"
}
```

**응답**: [참가자 추가](#7-참가자-추가)와 동일 (`actualArrivalTime` 필드 업데이트됨)

---

### 13. 예상 출발 시간 제안

**Endpoint**: `POST /api/plans/participants/{participantId}/suggest-departure`  
**인증**: 필요  
**설명**: 예상 이동 시간을 입력하면 출발 시간 자동 계산

**경로 파라미터**
- `participantId` (Long): 참가자 ID

**요청 본문**
```json
{
  "expectedTravelTimeMinutes": 45
}
```

**응답**: [참가자 추가](#7-참가자-추가)와 동일 (`expectedDepartureTime`, `expectedTravelTimeMinutes` 업데이트)

---

### 14. 지각 벌금 조회

**Endpoint**: `GET /api/plans/participants/{participantId}/late-fine`  
**인증**: 필요  
**설명**: 참가자의 지각 벌금 계산 결과 조회

**경로 파라미터**
- `participantId` (Long): 참가자 ID

**응답**
```json
{
  "success": true,
  "data": 5000,
  "message": null
}
```

---

### 15. 장소 확정

**Endpoint**: `POST /api/plans/{planId}/confirm-place`  
**인증**: 필요  
**설명**: 약속 장소 확정 (생성자만 가능)

**경로 파라미터**
- `planId` (Long): 약속 ID

**요청 본문**
```json
{
  "placeName": "강남역 2번 출구",
  "latitude": 37.497952,
  "longitude": 127.027619
}
```

**필드 설명**
- `placeName` (String, 필수): 장소명
- `latitude` (Double, 선택): 위도
- `longitude` (Double, 선택): 경도

**응답**: [약속 단건 조회](#2-약속-단건-조회)와 동일

---

### 16. 약속 최종 확정

**Endpoint**: `POST /api/plans/{planId}/confirm`  
**인증**: 필요  
**설명**: 약속 최종 확정 (상태 → `CONFIRMED`, AI 임베딩 생성 트리거)

**경로 파라미터**
- `planId` (Long): 약속 ID

**응답**
```json
{
  "success": true,
  "data": { /* Plan 객체 */ },
  "message": "약속이 최종 확정되었습니다."
}
```

---

### 17. 약속 완료 (수동)

**Endpoint**: `POST /api/plans/{planId}/complete`  
**인증**: 필요  
**설명**: 약속 수동 완료 처리 (생성자만 가능) - 자동 완료는 모든 멤버 도착 시 트리거됨

**경로 파라미터**
- `planId` (Long): 약속 ID

**응답**
```json
{
  "success": true,
  "data": { /* Plan 객체 */ },
  "message": "약속이 완료되었습니다."
}
```

---

## Review (후기) API

### 1. 후기 작성

**Endpoint**: `POST /api/reviews`  
**인증**: 필요  
**설명**: 약속 완료 후 후기 작성 (약속이 `COMPLETED` 상태일 때만 가능, 1인 1후기)

**요청 본문**
```json
{
  "planId": 10,
  "title": "즐거운 저녁이었어요!",
  "content": "모두 정시에 도착해서 재미있게 놀았습니다. 다음에 또 만나요~"
}
```

**필드 설명**
- `planId` (Long, 필수): 약속 ID
- `title` (String, 필수, 최대 100자): 후기 제목
- `content` (String, 필수, 최대 500자): 후기 내용

**응답**
```json
{
  "success": true,
  "data": {
    "id": 25,
    "title": "즐거운 저녁이었어요!",
    "content": "모두 정시에 도착해서 재미있게 놀았습니다. 다음에 또 만나요~",
    "planId": 10,
    "planTitle": "홍대 저녁 식사",
    "authorName": "홍길동",
    "authorId": 5,
    "createdAt": "2025-01-21T10:30:00",
    "updatedAt": null,
    "replies": []
  },
  "message": "후기가 작성되었습니다."
}
```

---

### 2. 후기 단건 조회

**Endpoint**: `GET /api/reviews/{reviewId}`  
**인증**: 불필요  
**설명**: 후기 상세 내용 조회 (댓글 포함)

**경로 파라미터**
- `reviewId` (Long): 후기 ID

**응답**: [후기 작성](#1-후기-작성)과 동일한 `ReviewDTO` 객체

---

### 3. 약속별 후기 목록 조회

**Endpoint**: `GET /api/reviews/plan/{planId}`  
**인증**: 불필요  
**설명**: 특정 약속에 대한 모든 후기 조회

**경로 파라미터**
- `planId` (Long): 약속 ID

**응답**
```json
{
  "success": true,
  "data": [
    {
      "id": 25,
      "title": "즐거운 저녁이었어요!",
      "content": "모두 정시에 도착해서 재미있게 놀았습니다.",
      "planId": 10,
      "planTitle": "홍대 저녁 식사",
      "authorName": "홍길동",
      "authorId": 5,
      "createdAt": "2025-01-21T10:30:00",
      "updatedAt": null,
      "replies": [
        {
          "id": 100,
          "content": "저도 재밌었어요!",
          "authorName": "김철수",
          "authorId": 8,
          "createdAt": "2025-01-21T11:00:00",
          "updatedAt": null
        }
      ]
    }
  ],
  "message": null
}
```

---

### 4. 내가 작성한 후기 목록 조회 (페이징)

**Endpoint**: `GET /api/reviews/my`  
**인증**: 필요  
**설명**: 로그인한 사용자가 작성한 모든 후기 조회 (페이징 지원)

**쿼리 파라미터** (선택)
- `page` (int, 기본값: 0): 페이지 번호 (0부터 시작)
- `size` (int, 기본값: 20): 페이지 크기

**예시 요청**
```
GET /api/reviews/my?page=0&size=10
```

**응답**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 25,
        "title": "즐거운 저녁이었어요!",
        "content": "모두 정시에 도착해서...",
        "planId": 10,
        "planTitle": "홍대 저녁 식사",
        "authorName": "홍길동",
        "authorId": 5,
        "createdAt": "2025-01-21T10:30:00",
        "updatedAt": null,
        "replies": []
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 5,
    "totalPages": 1,
    "last": true,
    "first": true
  },
  "message": null
}
```

---

### 5. 후기 수정

**Endpoint**: `PUT /api/reviews/{reviewId}`  
**인증**: 필요  
**설명**: 후기 수정 (작성자 본인만 가능)

**경로 파라미터**
- `reviewId` (Long): 후기 ID

**요청 본문**
```json
{
  "title": "즐거운 저녁이었어요! (수정)",
  "content": "정말 재미있었습니다. 다음에 또 만나요~"
}
```

**필드 설명**
- `title` (String, 선택, 최대 100자): 수정할 제목
- `content` (String, 선택, 최대 500자): 수정할 내용

**응답**: [후기 작성](#1-후기-작성)과 동일

---

### 6. 후기 삭제

**Endpoint**: `DELETE /api/reviews/{reviewId}`  
**인증**: 필요  
**설명**: 후기 삭제 (작성자 본인만 가능)

**경로 파라미터**
- `reviewId` (Long): 후기 ID

**응답**
```json
{
  "success": true,
  "data": null,
  "message": "후기가 삭제되었습니다."
}
```

---

## Reply (댓글) API

### 1. 댓글 작성

**Endpoint**: `POST /api/replies/review/{reviewId}`  
**인증**: 필요  
**설명**: 후기에 댓글 작성

**경로 파라미터**
- `reviewId` (Long): 후기 ID

**요청 본문**
```json
{
  "content": "저도 재밌었어요!"
}
```

**필드 설명**
- `content` (String, 필수, 최대 500자): 댓글 내용

**응답**
```json
{
  "success": true,
  "data": {
    "id": 100,
    "content": "저도 재밌었어요!",
    "authorName": "김철수",
    "authorId": 8,
    "createdAt": "2025-01-21T11:00:00",
    "updatedAt": null
  },
  "message": "댓글이 작성되었습니다."
}
```

---

### 2. 댓글 수정

**Endpoint**: `PUT /api/replies/{replyId}`  
**인증**: 필요  
**설명**: 댓글 수정 (작성자 본인만 가능)

**경로 파라미터**
- `replyId` (Long): 댓글 ID

**요청 본문**
```json
{
  "content": "저도 정말 재밌었어요!"
}
```

**응답**: [댓글 작성](#1-댓글-작성)과 동일

---

### 3. 댓글 삭제

**Endpoint**: `DELETE /api/replies/{replyId}`  
**인증**: 필요  
**설명**: 댓글 삭제 (작성자 본인만 가능)

**경로 파라미터**
- `replyId` (Long): 댓글 ID

**응답**
```json
{
  "success": true,
  "data": null,
  "message": "댓글이 삭제되었습니다."
}
```

---

## 에러 코드

### 4xx 클라이언트 오류
- **400 Bad Request**: 잘못된 요청 (필수 필드 누락, 형식 오류 등)
  - 예: `"잘못된 날짜 형식입니다."`
  - 예: `"약속이 종료된 후에만 후기를 작성할 수 있습니다."`
- **401 Unauthorized**: 인증되지 않은 사용자
  - 예: `"인증되지 않은 사용자입니다."`
- **403 Forbidden**: 권한 없음
  - 예: `"플랜 생성자만 수정/삭제할 수 있습니다."`
  - 예: `"작성자만 수정/삭제할 수 있습니다."`
- **404 Not Found**: 리소스를 찾을 수 없음
  - 예: `"해당 플랜을 찾을 수 없습니다."`
  - 예: `"해당 후기를 찾을 수 없습니다."`

### 5xx 서버 오류
- **500 Internal Server Error**: 서버 내부 오류

---

## Flutter 구현 예시 (Dart)

### API 클라이언트 기본 설정

```dart
class ApiClient {
  static const String baseUrl = 'http://your-server.com/api';
  final Dio _dio;

  ApiClient() : _dio = Dio(BaseOptions(
    baseUrl: baseUrl,
    connectTimeout: Duration(seconds: 10),
    receiveTimeout: Duration(seconds: 10),
  )) {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        // JWT 토큰 추가
        final token = getToken(); // SharedPreferences 등에서 가져오기
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (DioError e, handler) {
        // 에러 처리
        print('API Error: ${e.message}');
        return handler.next(e);
      },
    ));
  }

  Future<List<Plan>> getPlans() async {
    final response = await _dio.get('/plans');
    return (response.data['data'] as List)
        .map((json) => Plan.fromJson(json))
        .toList();
  }

  Future<Review> createReview({
    required int planId,
    required String title,
    required String content,
  }) async {
    final response = await _dio.post('/reviews', data: {
      'planId': planId,
      'title': title,
      'content': content,
    });
    return Review.fromJson(response.data['data']);
  }
}
```

### 모델 클래스 예시

```dart
class Plan {
  final int id;
  final String title;
  final String date;
  final String time;
  final String? location;
  final double? placeLatitude;
  final double? placeLongitude;
  final List<Participant> participants;

  Plan({
    required this.id,
    required this.title,
    required this.date,
    required this.time,
    this.location,
    this.placeLatitude,
    this.placeLongitude,
    required this.participants,
  });

  factory Plan.fromJson(Map<String, dynamic> json) {
    return Plan(
      id: json['id'],
      title: json['title'],
      date: json['date'],
      time: json['time'],
      location: json['location'],
      placeLatitude: json['placeLatitude']?.toDouble(),
      placeLongitude: json['placeLongitude']?.toDouble(),
      participants: (json['participants'] as List)
          .map((p) => Participant.fromJson(p))
          .toList(),
    );
  }
}

class Review {
  final int id;
  final String title;
  final String content;
  final int planId;
  final String planTitle;
  final String authorName;
  final int authorId;
  final String createdAt;
  final String? updatedAt;
  final List<Reply> replies;

  Review({
    required this.id,
    required this.title,
    required this.content,
    required this.planId,
    required this.planTitle,
    required this.authorName,
    required this.authorId,
    required this.createdAt,
    this.updatedAt,
    required this.replies,
  });

  factory Review.fromJson(Map<String, dynamic> json) {
    return Review(
      id: json['id'],
      title: json['title'],
      content: json['content'],
      planId: json['planId'],
      planTitle: json['planTitle'],
      authorName: json['authorName'],
      authorId: json['authorId'],
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
      replies: (json['replies'] as List)
          .map((r) => Reply.fromJson(r))
          .toList(),
    );
  }
}
```

---

## 주의사항

1. **약속 완료 후에만 후기 작성 가능**: `Plan.status == COMPLETED` 체크
2. **1인 1후기 제한**: 같은 약속에 중복 후기 작성 불가
3. **권한 검증**: 생성자/작성자만 수정/삭제 가능한 API 확인
4. **날짜 형식**: ISO 8601 형식 (`yyyy-MM-ddTHH:mm:ss`) 필수
5. **페이징**: `/api/reviews/my`는 Spring Data Pageable 응답 구조 참고
6. **모든 멤버 도착 시 자동 완료**: 마지막 멤버 도착 기록 시 약속 자동 `COMPLETED` 처리

---

**문서 작성**: 2025-01-14  
**버전**: 1.0

