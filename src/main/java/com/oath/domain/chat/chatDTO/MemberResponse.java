package com.oath.domain.chat.chatDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long memberId;
    private String username;
    private String profileImageUrl;
}
