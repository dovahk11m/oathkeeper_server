package com.oath.domain.groups.groupDTO;

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
    private String message; // 클라이언트에 전달할 추가 메시지
}
