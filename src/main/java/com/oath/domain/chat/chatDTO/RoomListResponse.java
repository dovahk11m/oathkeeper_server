package com.oath.domain.chat.chatDTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomListResponse {
    private Long chatRoomId;
    private String chatRoomName;
    private String lastMessage;
    private LocalDateTime lastMessageSentAt;
    private Long unreadCount;
}
