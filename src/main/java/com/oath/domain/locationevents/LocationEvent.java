package com.oath.domain.locationevents;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long participantId; // FK plan_participants.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    // TODO: 추후 POINT 타입으로 변경 필요
    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
