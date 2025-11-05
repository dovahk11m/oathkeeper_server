package com.oath.domain.members.memberEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberSignupEvent {
    private final String email;
    private final String username;
    private final String verificationToken;
}
