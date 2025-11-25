package com.oath.domain.metrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AiGroupSummaryRequest {
    private Long groupId;
    private List<Long> planIds;
}
