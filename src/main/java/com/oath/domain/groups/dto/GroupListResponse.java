package com.oath.domain.groups.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupListResponse {
    private Long groupId;
    private String groupName;
    private Long chatRoomId;
    private String lastMessage;
    private LocalDateTime lastMessageSentAt;
    private Long unreadCount;
}