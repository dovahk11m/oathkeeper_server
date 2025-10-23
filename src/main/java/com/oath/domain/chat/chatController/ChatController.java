package com.oath.domain.chat.chatController;

import com.oath.common.auth.StompPrincipal;
import com.oath.domain.chat.chatDTO.MessageRequest;
import com.oath.domain.chat.chatDTO.MessageResponse;
import com.oath.domain.chat.chatService.ChatService;
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
     * @param roomId         메시지를 보낼 채팅방의 ID
     * @param request        클라이언트가 보낸 메시지 데이터
     * @param principal      StompInterceptor에서 설정한 사용자 인증 정보
     */
    @MessageMapping("/chat/rooms/{roomId}/message")
    public void message(
            @DestinationVariable Long roomId,
            MessageRequest request,
            StompPrincipal principal
    ) {
        // 1. 서비스를 호출하여 메시지를 DB에 저장하고, 응답 DTO를 생성합니다.
        MessageResponse response = chatService.processAndSaveMessage(request, roomId, principal.getName());

        // 2. 해당 채팅방을 구독하고 있는 모든 클라이언트에게 메시지를 브로드캐스팅합니다.
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, response);
    }
}