package com.oath.domain.chat.chatDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 새로운 채팅방을 생성할 때 사용하는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class RoomCreateRequest {
    private Long groupId;
    private String name;
}
