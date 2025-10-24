package com.oath.domain.chats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 서버가 클라이언트에게 메시지를 전달(브로드캐스팅)하거나,
 * 이전 대화 내용을 응답할 때 사용하는 DTO입니다.
 */
@Getter
@Builder
public class ChatResponse {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private String content;
    private Long planId; // 플랜 관련 메시지 식별용

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime sentAt;

    /**
     * Chat 엔티티를 ChatResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * @param chat 변환할 Chat 엔티티
     * @return 생성된 ChatResponse DTO
     */
    public static ChatResponse from(Chat chat) {
        return ChatResponse.builder()
                .messageId(chat.getId())
                .senderId(chat.getSender().getId())
                .senderName(chat.getSender().getUsername())
                .senderProfileImageUrl(chat.getSender().getProfileImageUrl())
                .content(chat.getContent())
                .planId(chat.getPlanId())
                .sentAt(chat.getSentAt())
                .build();
    }
}
