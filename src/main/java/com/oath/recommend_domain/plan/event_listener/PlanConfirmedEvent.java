package com.oath.recommend_domain.plan.event_listener;

import lombok.Data;

@Data
public class PlanConfirmedEvent {

    private final Long planId;

    public PlanConfirmedEvent(Long planId) {
        this.planId = planId;
    }
}
