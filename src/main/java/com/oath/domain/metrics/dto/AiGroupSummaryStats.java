package com.oath.domain.metrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiGroupSummaryStats {
    private Integer totalPlansCompleted;
    private Integer totalLateMinutes;
    private Integer totalOnTimeArrivals;
    private Double totalTravelDistance;
    private Integer totalTravelTime;
}
