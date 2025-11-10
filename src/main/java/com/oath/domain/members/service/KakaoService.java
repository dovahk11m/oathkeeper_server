package com.oath.domain.members.service;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception500;
import com.oath.domain.members.dto.AccessTokenDto;
import com.oath.domain.members.dto.KakaoProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    // 네이티브 앱 플로우에서는 redirect-uri가 필수가 아니므로, 관련 의존성을 제거합니다.
    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;


    public AccessTokenDto getAccessToken(String code) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("code", code);
        // 네이티브 앱의 인가 코드를 사용할 때는 redirect_uri를 포함하지 않는 것이 더 안정적입니다.
        params.add("redirect_uri", kakaoRedirectUri);

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("카카오 액세스 토큰 요청 실패: 상태 코드 = {}, 응답 본문 = {}", response.getStatusCode(), response.getStatusText());
                    throw new Exception400("유효하지 않은 카카오 인가 코드입니다. / 응답: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("카카오 서버 오류: 상태 코드 = {}, 응답 본문 = {}", response.getStatusCode(), response.getStatusText());
                    throw new Exception500("카카오 서버에 문제가 발생했습니다.");
                })
                .body(AccessTokenDto.class);
    }

    public KakaoProfileDto getKakaoProfile(String token) {
        RestClient restClient = RestClient.create();

        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("카카오 프로필 요청 실패: 상태 코드 = {}, 응답 본문 = {}", response.getStatusCode(), response.getStatusText());
                    throw new Exception400("유효하지 않은 카카오 액세스 토큰입니다.");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("카카오 서버 오류: 상태 코드 = {}, 응답 본문 = {}", response.getStatusCode(), response.getStatusText());
                    throw new Exception500("카카오 서버에 문제가 발생했습니다.");
                })
                .body(KakaoProfileDto.class);
    }
}
