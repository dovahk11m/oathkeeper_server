# 약속지킴이 알람 시스템 구조

## 📌 개요

약속지킴이 프로젝트의 알람 시스템은 **전략 패턴 + 팩토리 패턴**을 활용하여 다양한 알람 전송 방식을 지원합니다.

---

## 🏗️ 아키텍처

```
Plan Service (비즈니스 로직)
    ↓ Spring Event 발행
Event Listener (ArrivalListener, DepartureListener, LateListener)
    ↓ AlarmFactory 호출
AlarmFactory (전략 선택)
    ↓ AlarmSender 구현체 반환
AlarmSender 구현체 (실제 전송)
    - AlarmWebSocket (실시간 푸시)
    - AlarmLocal (DB 저장)
    - AlarmComposite (WebSocket + DB)
    - AlarmEmail (이메일)
    - AlarmFCM (모바일 푸시 - TODO)
```

---

## 📡 알람 전송 방식

### 1. **WebSocket (실시간 푸시)** ⭐ 권장
- **타입**: `WEBSOCKET` or `WS`
- **용도**: 웹/앱에서 실시간 알람 수신
- **구독 경로**: `/topic/alarms/{userEmail}`
- **장점**: 즉시 전달, 서버 부하 적음

**클라이언트 연결 예시**:
```javascript
// 1. STOMP 연결
const socket = new SockJS('http://localhost:8080/ws-stomp');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 2. 개인 알람 채널 구독
    stompClient.subscribe('/topic/alarms/user@example.com', function(message) {
        const alarm = JSON.parse(message.body);
        console.log('알람 수신:', alarm.subject, alarm.content);
        // UI에 알람 표시
    });
});
```

---

### 2. **Local (인앱 알림 DB 저장)**
- **타입**: `LOCAL`
- **용도**: 오프라인 시 나중에 읽을 수 있도록 DB 저장
- **테이블**: `alarm_local_entity`
- **장점**: 알람 이력 관리, 읽음 처리 가능

---

### 3. **Composite (복합 전송)** ⭐ 현재 기본값
- **타입**: `COMPOSITE` or `MULTI`
- **동작**: WebSocket(실시간) + Local(DB 저장) 동시 수행
- **장점**: 온라인 사용자는 즉시 받고, 오프라인 시 나중에 확인 가능

---

### 4. **Email (이메일 전송)**
- **타입**: `EMAIL`
- **용도**: 중요한 알람을 이메일로도 전송
- **SMTP 설정**: `application-local.yml`의 `spring.mail` 참조

---

### 5. **FCM (모바일 푸시)** - 🚧 구현 필요
- **타입**: `FCM` or `PUSH`
- **용도**: 모바일 앱 푸시 알림
- **상태**: 기본 구조만 있음, Firebase 설정 필요

---

## ⚙️ 설정 방법

### `application-local.yml`에서 알람 방식 변경:

```yaml
notification:
  policy:
    default: COMPOSITE  # 기본 알람 방식
    on-arrival: WEBSOCKET  # 도착 알람은 WebSocket만
    on-departure: COMPOSITE  # 출발 알람은 복합 전송
    on-late: EMAIL  # 지각 알람은 이메일로
```

---

## 🔔 알람 이벤트 종류

### 1. **출발 알람 (DepartureEvent)**
- **발생 시점**: 참가자가 출발 버튼을 눌렀을 때
- **수신 대상**: 다른 참가자들
- **메시지**: "OOO님이 출발했습니다."

### 2. **도착 알람 (ArrivalEvent)**
- **발생 시점**: 참가자가 약속 장소에 도착했을 때
- **수신 대상**: 다른 참가자들
- **메시지**: "OOO님이 약속 장소에 도착했습니다."

### 3. **지각 알람 (LateEvent)**
- **발생 시점**: 참가자가 약속 시간보다 늦게 도착했을 때
- **수신 대상**: 다른 참가자들
- **메시지**: "OOO님이 X분 지각했습니다."

---

## 🧪 테스트 방법

### 1. WebSocket 연결 정보 조회
```bash
GET /api/alarms/ws-info
Authorization: Bearer {JWT_TOKEN}
```

**응답**:
```json
{
  "success": true,
  "data": {
    "endpoint": "/ws-stomp",
    "subscribe": "/topic/alarms/{userEmail}",
    "description": "WebSocket 연결 후 /topic/alarms/{본인이메일}을 구독하세요"
  }
}
```

### 2. 출발 알람 테스트
```bash
POST /api/plans/participants/{participantId}/departure
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "time": "2025-10-28T10:00:00"
}
```

→ 다른 참가자들에게 WebSocket으로 알람 전송됨

---

## 📂 주요 파일 구조

```
src/main/java/com/oath/domain/
├── alarms/
│   ├── AlarmFactory.java           # 전략 선택
│   ├── AlarmSender.java            # 전송 인터페이스
│   ├── AlarmDTO.java               # 알람 데이터
│   ├── strategies/
│   │   ├── AlarmWebSocket.java     # WebSocket 전송 ⭐
│   │   ├── AlarmLocal.java         # DB 저장
│   │   ├── AlarmComposite.java     # 복합 전송 ⭐
│   │   ├── AlarmEmail.java         # 이메일 전송
│   │   └── AlarmFCM.java           # FCM 전송 (TODO)
│   └── controller/
│       └── AlarmController.java    # WebSocket 정보 제공
│
└── plan/
    ├── event/
    │   ├── ArrivalEvent.java
    │   ├── DepartureEvent.java
    │   └── LateEvent.java
    └── event/listener/
        ├── ArrivalListener.java     # 도착 이벤트 처리
        ├── DepartureListener.java   # 출발 이벤트 처리
        └── LateListener.java        # 지각 이벤트 처리
```

---

## 🚀 향후 개선 사항

### 1. FCM 구현
- Firebase Admin SDK 추가
- FCM 토큰 저장 테이블 생성
- 모바일 앱 푸시 알림 완성

### 2. 알람 읽음 처리
- 읽지 않은 알람 개수 조회 API
- 알람 읽음 처리 API
- 알람 삭제 API

### 3. 알람 설정
- 사용자별 알람 on/off 설정
- 방해 금지 시간 설정
- 알람 우선순위 설정

---

## 📖 참고

- **WebSocket 설정**: `WebSocketConfig.java`
- **Spring Event**: `@TransactionalEventListener` 사용
- **비동기 처리**: `@Async` 적용으로 성능 최적화
- **OSIV**: `false`로 설정하여 트랜잭션 분리

---

**작성일**: 2025-10-28  
**작성자**: GitHub Copilot

