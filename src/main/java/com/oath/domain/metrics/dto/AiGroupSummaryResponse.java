package com.oath.domain.metrics.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // JsonProperty import 추가
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AiGroupSummaryResponse {
    private Boolean success;
    private ResponseData data;
    private String message;
    private List<String> warnings;

    @Getter
    @Setter
    @Builder
    public static class ResponseData {
        @JsonProperty("group_summary") // JSON 필드명 매핑
        private AiGroupSummaryStats groupSummary; // camelCase로 변경

        @JsonProperty("text_summary") // JSON 필드명 매핑
        private String textSummary; // camelCase로 변경
    }
}
