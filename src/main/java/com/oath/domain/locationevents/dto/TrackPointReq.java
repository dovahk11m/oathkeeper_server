package com.oath.domain.locationevents.dto;

import java.time.LocalDateTime;

public record TrackPointReq(
        Double lat,
        Double lng,
        LocalDateTime ts,
        Float speedMps,
        Float accuracyM,
        String source,
        Boolean isMock
) {}
