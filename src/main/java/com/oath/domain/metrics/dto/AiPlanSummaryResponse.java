package com.oath.domain.metrics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPlanSummaryResponse {

    private Boolean success;
    private PlanSummaryData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanSummaryData {
        @JsonProperty("plan_id")
        private Long planId;

        @JsonProperty("text_summary")
        private String textSummary;
    }
}
