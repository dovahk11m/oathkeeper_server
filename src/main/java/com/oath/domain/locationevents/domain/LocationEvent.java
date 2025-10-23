// com/oath/domain/locationevents/domain/LocationEvent.java
package com.oath.domain.locationevents.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_events_tb",
        indexes = @Index(name = "idx_ev_part_ts", columnList = "participantId, ts"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long participantId;

    // DEPARTURE/ARRIVAL/STOPPED/RESUMED
    @Column(nullable = false, length = 12)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime ts;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
