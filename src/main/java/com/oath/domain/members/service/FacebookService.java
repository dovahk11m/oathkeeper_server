package com.oath.domain.members.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.domain.members.dto.AccessTokenDto;
import com.oath.domain.members.dto.FacebookProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@Service
public class FacebookService {

    @Value("${oauth.facebook.client-id}")
    private String facebookClientId;

    @Value("${oauth.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${oauth.facebook.redirect-uri}")
    private String facebookRedirectUri;


    public AccessTokenDto getAccessToken(String code) {
        RestClient restClient = RestClient.builder()
                .messageConverters(converters -> {
                    for (var converter : converters) {
                        if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                            // text/javascript 도 JSON으로 처리
                            jacksonConverter.setSupportedMediaTypes(List.of(
                                    MediaType.APPLICATION_JSON,
                                    new MediaType("text", "javascript")
                            ));
                        }
                    }
                })
                .build();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", facebookClientId);
        params.add("client_secret", facebookClientSecret);
        params.add("redirect_uri", facebookRedirectUri);
        params.add("code", code);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://graph.facebook.com/v17.0/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .toEntity(AccessTokenDto.class);

        ObjectMapper mapper = new ObjectMapper();

            return response.getBody();

    }



    public FacebookProfileDto getFacebookProfile(String token) {
        RestClient restClient = RestClient.builder()
                .messageConverters(converters -> {
                    for (var converter : converters) {
                        if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                            // text/javascript 도 JSON으로 처리
                            jacksonConverter.setSupportedMediaTypes(List.of(
                                    MediaType.APPLICATION_JSON,
                                    new MediaType("text", "javascript")
                            ));
                        }
                    }
                })
                .build();

        ResponseEntity<FacebookProfileDto> response = restClient.get()
                .uri("https://graph.facebook.com/me?fields=id,name,email&format=json")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(FacebookProfileDto.class);
        System.out.println("profile JSON" + response.getBody());
        return response.getBody();
    }
}
