package com.oath.domain.plan.event;

import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DepartureEvent {
    private final Plan plan;
    private final Participant departedParticipant;
    private final List<Participant> otherParticipants;
    private final LocalDateTime publishedAt;
    private final AlarmType alarmType;

    public DepartureEvent(Plan plan, Participant departedParticipant, List<Participant> otherParticipants, AlarmType alarmType) {
        this.plan = plan;
        this.departedParticipant = departedParticipant;
        this.otherParticipants = otherParticipants;
        this.publishedAt = LocalDateTime.now();
        this.alarmType = alarmType;
    }
}

