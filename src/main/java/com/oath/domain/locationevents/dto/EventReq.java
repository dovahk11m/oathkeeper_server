package com.oath.domain.locationevents.dto;

import java.time.LocalDateTime;

public record EventReq(
        Long participantId,
        String eventType,
        Double lat,
        Double lng,
        LocalDateTime ts
) {}
