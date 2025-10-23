package com.oath.domain.chat;

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
     * 클라이언트가 /app/chat/rooms/{roomId}/message 경로로 메시지를 보내면 이 메서드가 처리합니다.
     *
     * @param groupId        메시지를 보낼 그룹(채팅방)의 ID
     * @param request        클라이언트가 보낸 메시지 데이터
     * @param principal      StompInterceptor에서 설정한 사용자 인증 정보
     */
    @MessageMapping("/chat/groups/{groupId}/message") // [수정] 경로를 group 기반으로 변경
    public void message(
            @DestinationVariable Long groupId, // [수정] 파라미터를 groupId로 변경
            ChatRequest request,
            StompPrincipal principal
    ) {
        // 1. 서비스를 호출하여 메시지를 DB에 저장하고, 응답 DTO를 생성합니다.
        ChatResponse response = chatService.processAndSaveMessage(request, groupId, principal.getName());

        // 2. 해당 채팅방을 구독하고 있는 모든 클라이언트에게 메시지를 브로드캐스팅합니다.
        messagingTemplate.convertAndSend("/topic/chat/groups/" + groupId, response); // [수정] 토픽 경로도 group 기반으로 변경
    }
}