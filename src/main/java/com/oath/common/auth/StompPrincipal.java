package com.oath.common.auth;

import com.oath.domain.members.MemberRole;
import lombok.Getter;

import java.security.Principal;

/**
 * WebSocket 세션에 저장될 인증된 사용자 정보를 나타내는 객체입니다.
 */
@Getter
public class StompPrincipal implements Principal {

    private final String email;
    private final MemberRole role;

    public StompPrincipal(String email, MemberRole role) {
        this.email = email;
        this.role = role;
    }

    @Override
    public String getName() {
        return this.email;
    }
}