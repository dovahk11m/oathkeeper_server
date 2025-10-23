package com.oath.domain.metrics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class AiMetricsPayload {
    @JsonProperty("plan_id")        public Long planId;
    @JsonProperty("member_id")      public Long memberId;
    @JsonProperty("distance_km")    public Double distanceKm;
    @JsonProperty("travel_minutes") public Integer travelMinutes;
    @JsonProperty("late_minutes")   public Integer lateMinutes;
    @JsonProperty("wait_minutes")   public Integer waitMinutes;
    @JsonProperty("created_at")     public LocalDateTime createdAt;

    public AiMetricsPayload(Long planId, Long memberId, Double distanceKm, Integer travelMinutes,
                            Integer lateMinutes, Integer waitMinutes, LocalDateTime createdAt) {
        this.planId = planId;
        this.memberId = memberId;
        this.distanceKm = distanceKm;
        this.travelMinutes = travelMinutes;
        this.lateMinutes = lateMinutes;
        this.waitMinutes = waitMinutes;
        this.createdAt = createdAt;
    }
}
