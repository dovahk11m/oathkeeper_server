package com.oath.domain.plan.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlanCompletedEvent {
    private final Long planId;
}
