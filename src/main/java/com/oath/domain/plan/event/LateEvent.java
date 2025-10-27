package com.oath.domain.plan.event;

import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class LateEvent {
    private final Plan plan;
    private final Participant lateParticipant;
    private final List<Participant> otherParticipants;
    private final Integer lateMinutes;
    private final LocalDateTime publishedAt;
    private final AlarmType alarmType;

    public LateEvent(Plan plan, Participant lateParticipant, List<Participant> otherParticipants, Integer lateMinutes, AlarmType alarmType) {
        this.plan = plan;
        this.lateParticipant = lateParticipant;
        this.otherParticipants = otherParticipants;
        this.lateMinutes = lateMinutes;
        this.publishedAt = LocalDateTime.now();
        this.alarmType = alarmType;
    }
}

