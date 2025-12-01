package com.oath.domain.groups.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberResponse {
    private Long memberId;
    private String username;
    private String profileImageUrl;
}