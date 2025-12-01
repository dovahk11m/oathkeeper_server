package com.oath.domain.plan.request;

import com.oath.domain.plan.MovementStatus;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.domain.Participant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class ParticipantResponse {

    private Long id;
    private Long memberId;
    private ParticipantStatus participantStatus;
    private MovementStatus movementStatus;
    private String memberNickname;
    private String transportMethod;
    private String startAddress;
    private Double startLatitude;
    private Double startLongitude;
    private Integer expectedTravelTimeMinutes;
    private LocalDateTime expectedDepartureTime;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualArrivalTime;
    private Integer timeBurdenMinutes;
    private String departureFailureReason;

    public ParticipantResponse(Participant pm) {
        if (pm == null) return;
        this.id = pm.getId();
        if (pm.getMember() != null) this.memberId = pm.getMember().getId();
        this.participantStatus = pm.getParticipantStatus();
        this.movementStatus = pm.getMovementStatus();
        this.memberNickname = pm.getMember().getUsername();
        this.transportMethod = pm.getTransportMethod();
        this.startAddress = pm.getStartAddress();
        this.startLatitude = pm.getStartLatitude();
        this.startLongitude = pm.getStartLongitude();
        this.expectedTravelTimeMinutes = pm.getExpectedTravelTimeMinutes();
        this.expectedDepartureTime = pm.getExpectedDepartureTime();
        this.actualDepartureTime = pm.getActualDepartureTime();
        this.actualArrivalTime = pm.getActualArrivalTime();
        this.timeBurdenMinutes = pm.getTimeBurdenMinutes();
        this.departureFailureReason = pm.getDepartureFailureReason();
    }

    public static ParticipantResponse of(Participant pm) {
        return new ParticipantResponse(pm);
    }
}
