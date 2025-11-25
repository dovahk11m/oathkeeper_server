package com.oath.domain.metrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AiGroupSummaryResponse {
    private Boolean success; // AI 서버 응답의 성공 여부
    private ResponseData data; // 'data' 필드 추가
    private String message; // AI 서버 응답 메시지 (예: 409 Conflict 시)
    private List<String> warnings; // AI 서버 응답 경고 목록

    @Getter // ResponseData에도 @Getter 추가
    @Setter
    @Builder
    public static class ResponseData { // 내부 클래스로 ResponseData 정의
        private AiGroupSummaryStats group_summary; // AI 서버 응답 필드명과 일치
        private String text_summary; // AI 서버 응답 필드명과 일치
    }
}
