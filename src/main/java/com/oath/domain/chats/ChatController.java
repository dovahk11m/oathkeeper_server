package com.oath.domain.chats;

import com.oath.common.auth.StompPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    /**
     * 클라이언트가 /app/chat/groups/{groupId}/message 경로로 메시지를 보내면 처리
     *
     * @param groupId   메시지를 보낼 그룹(채팅방)의 ID
     * @param request   클라이언트가 보낸 메시지 데이터
     * @param principal StompInterceptor에서 설정한 사용자 인증 정보
     */
    @MessageMapping("/chat/groups/{groupId}/message")
    public void message(
            @DestinationVariable Long groupId,
            ChatRequest request,
            StompPrincipal principal
    ) {
        ChatResponse response = chatService.processAndSaveMessage(
                request,
                groupId,
                principal.getName()
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/groups/" + groupId,
                response
        );
    }
}

/*
 실시간 채팅 API 명세 (WebSocket/STOMP)

프로토콜: STOMP over WebSocket



# 1. 연결 (Connect)
- URL: `ws://<서버_주소>/ws` (Postman 등 테스트 도구용) 또는 `ws://<서버_주소>/ws-stomp` (웹 브라우저의 SockJS용)
- 설명: 실시간 채팅을 위해 서버와 WebSocket 연결을 수립합니다.
- Connection Headers:
  - `StompInterceptor`가 인증을 처리하므로, 연결 시 반드시 헤더에 JWT 토큰을 포함해야 합니다.

Authorization: Bearer <JWT_토큰>




# 2. 메시지 구독 (Subscribe)
- Destination: `/topic/chat/groups/{groupId}`
- 설명: 특정 그룹(채팅방)에 들어가면, 해당 그룹의 실시간 메시지를 수신하기 위해 이 경로를 구독해야 합니다.
- Path Variable:
  - `groupId`: 참여할 그룹의 ID
- 수신 메시지 형식: 서버는 이 경로로 `ChatResponse` 형식의 JSON 데이터를 브로드캐스팅합니다.
json
{
  "messageId": 10,
  "senderId": 1,
  "senderName": "user1",
  "senderProfileImageUrl": null,
  "content": "플러터에서 보내는 메시지입니다!",
  "planId": null,
  "sentAt": "2025-10-23 15:30:00"
}




# 3. 메시지 전송 (Send)
- Destination: `/app/chat/groups/{groupId}/message`
- 설명: 특정 그룹(채팅방)에 새로운 메시지를 전송합니다.
- Path Variable:
  - `groupId`: 메시지를 보낼 그룹의 ID
- Request Body: `ChatRequest` 형식의 JSON 데이터를 전송해야 합니다.
json
{
  "content": "플러터에서 보내는 메시지입니다!",
  "planId": 123
}

- 서버 동작:
  1. 서버는 이 메시지를 받아 `ChatService`를 통해 DB에 저장합니다.
  2. 저장이 완료되면, 해당 그룹을 구독 중인 모든 클라이언트(`_2. 메시지 구독_` 참고)에게 `ChatResponse` 형식의 메시지를 브로드캐스팅합니다.

*/