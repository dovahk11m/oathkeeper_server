package com.oath.domain.plan.event;

import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotDepartedEvent {
    private final Plan plan;
    private final Participant notDepartedParticipant;
    private final Integer remainingMinutes;
    private final LocalDateTime publishedAt;
    private final AlarmType alarmType;

    public NotDepartedEvent(Plan plan, Participant notDepartedParticipant, Integer remainingMinutes, AlarmType alarmType) {
        this.plan = plan;
        this.notDepartedParticipant = notDepartedParticipant;
        this.remainingMinutes = remainingMinutes;
        this.publishedAt = LocalDateTime.now();
        this.alarmType = alarmType;
    }
}

