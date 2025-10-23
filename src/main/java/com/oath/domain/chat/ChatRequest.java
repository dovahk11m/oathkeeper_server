package com.oath.domain.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클라이언트가 WebSocket을 통해 메시지를 전송할 때 사용하는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {
    private String content;
    private Long planId;
}
