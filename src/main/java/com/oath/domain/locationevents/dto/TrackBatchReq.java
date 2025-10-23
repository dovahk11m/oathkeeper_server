package com.oath.domain.locationevents.dto;

import java.util.List;

public record TrackBatchReq(
        Long participantId,
        List<TrackPointReq> points
) {}
