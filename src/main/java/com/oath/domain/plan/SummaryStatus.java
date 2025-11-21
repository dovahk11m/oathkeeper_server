package com.oath.domain.plan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SummaryStatus {
    NONE("요약 없음"),
    PENDING("요약 대기중"),
    PROCESSING("요약 처리중"), // IN_PROGRESS 대신 PROCESSING 사용
    COMPLETED("요약 완료"),
    FAILED("요약 실패");

    private final String description;
}
