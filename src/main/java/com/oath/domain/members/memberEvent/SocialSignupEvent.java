package com.oath.domain.members.memberEvent;

import com.oath.domain.members.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SocialSignupEvent {
    private final Member member;
}
