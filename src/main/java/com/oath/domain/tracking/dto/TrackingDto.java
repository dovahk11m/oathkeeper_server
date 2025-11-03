package com.oath.domain.tracking.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingDto {
    private long planId;
    private long memberId;   // 초기엔 클라 값 그대로. (이후 JWT로 대체)
    private double lat;
    private double lng;
    private Float accuracy;
    private Float speed;
    private Float heading;
    private Instant ts;      // UTC

    public void ensureDefaults() {
        if (ts == null) ts = Instant.now();
    }
}
