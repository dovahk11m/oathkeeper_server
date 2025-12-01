package com.oath.domain.members.service;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception500;
import com.oath.domain.members.dto.KakaoProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class KakaoService {

    private final WebClient webClient = WebClient.create("https://kapi.kakao.com");

    public KakaoProfileDto getKakaoProfile(String accessToken) {

        try {
            return webClient.get()
                    .uri("/v2/user/me")
                    .header(
                            "Authorization",
                            "Bearer " + accessToken
                    )
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            response -> {
                                log.error(
                                        "카카오 프로필 요청 실패: {}",
                                        response.statusCode()
                                );
                                return Mono.error(new Exception400("유효하지 않은 카카오 액세스 토큰입니다."));
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            response -> {
                                log.error(
                                        "카카오 서버 오류: {}",
                                        response.statusCode()
                                );
                                return Mono.error(new Exception500("카카오 서버에 문제가 발생했습니다."));
                            }
                    )
                    .bodyToMono(KakaoProfileDto.class)
                    .block(); // 동기 호출
        } catch (WebClientRequestException e) {
            log.error(
                    "카카오 API 요청 중 예외 발생",
                    e
            );
            throw new Exception500("카카오 서버 요청 중 오류가 발생했습니다.");
        }
    }
}