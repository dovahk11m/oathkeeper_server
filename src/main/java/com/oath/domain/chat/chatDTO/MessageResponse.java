package com.oath.domain.chat.chatDTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 서버가 클라이언트에게 메시지를 전달(브로드캐스팅)하거나,
 * 이전 대화 내용을 응답할 때 사용하는 DTO입니다.
 */
@Getter
@Builder
public class MessageResponse {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private String content;
    private Long planId; // 플랜 관련 메시지 식별용
    private LocalDateTime sentAt;
}
