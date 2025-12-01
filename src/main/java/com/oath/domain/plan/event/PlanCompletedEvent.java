package com.oath.domain.plan.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PlanCompletedEvent extends ApplicationEvent {
    private final Long planId;
    private final Long groupId;

    public PlanCompletedEvent(Object source, Long planId, Long groupId) {
        super(source);
        this.planId = planId;
        this.groupId = groupId;
    }
}
