// AI 서버와 통신하여 약속 요약 정보를 가져오는 서비스입니다.
package com.oath.domain.plan.service;

import com.oath.common.exception.Exception500;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AIService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public String getSummary(Long planId) {
        try {
            String url = aiServerUrl + "/summarize/" + planId;
            // AI 서버에 GET 요청을 보내고, 응답을 String으로 받습니다.
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // AI 서버 통신 중 오류 발생 시 500 에러를 반환합니다.
            throw new Exception500("AI 서버와 통신 중 오류가 발생했습니다.");
        }
    }
}
