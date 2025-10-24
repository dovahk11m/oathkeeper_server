package com.oath.domain.groups.groupEvent;

import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CreateGroupEvent {
    private final Group group;
    private final Member creator;
}
