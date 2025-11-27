package com.oath.domain.plan.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Plan 처리가 완료된 후, Group 관련 데이터를 업데이트해야 할 때 발생하는 이벤트입니다.
 */
@Getter
@RequiredArgsConstructor
public class GroupUpdateEvent {
    private final Long planId;
    private final Long groupId;
}
