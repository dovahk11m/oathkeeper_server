package com.oath.domain.tracking.model;

import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationTrack {
    private long id;
    private long planId;
    private long memberId;
    private double lat;
    private double lng;
    private Float accuracy;
    private Float speed;
    private Float heading;
    private Instant ts;
}