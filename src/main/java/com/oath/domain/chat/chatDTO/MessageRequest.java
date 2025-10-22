package com.oath.domain.chat.chatDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클라이언트가 WebSocket을 통해 메시지를 전송할 때 사용하는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class MessageRequest {
    private String content;
    private Long planId;
}
