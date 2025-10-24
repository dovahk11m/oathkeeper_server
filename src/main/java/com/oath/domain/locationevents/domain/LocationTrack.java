// com/oath/domain/locationevents/domain/LocationTrack.java
package com.oath.domain.locationevents.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_tracks_tb",
        indexes = @Index(name = "idx_part_ts", columnList = "participantId, ts"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationTrack {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Participant.id (느슨결합)
    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private LocalDateTime ts;

    // POINT 대신 lat/lng로 1차 구현
    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    private Float speedMps;
    private Float accuracyM;

    // FG/BG
    @Column(length = 2)
    private String source = "BG";

    private Boolean isMockLocation = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
