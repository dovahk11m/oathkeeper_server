package com.oath.domain.groups.dto;

import com.oath.domain.groups.SummaryStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GroupSummaryResponse {
    private Long groupId;
    private String summary;
    private SummaryStatus status;
    private LocalDateTime lastUpdatedAt;
    private String reason; // 요약 상태(PENDING, FAILED 등)에 대한 부가 설명
}
