package com.oath.domain.map.google.common;

import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@Slf4j
public class GoogleMapApiClient {

    private final RestTemplate restTemplate;
    private final String endpoint;
    private final String apiKey;
    private final String fieldMask;

    public GoogleMapApiClient(RestTemplate restTemplate,
                              @Value("${map.google.matrix-endpoint}") String endpoint,
                              @Value("${map.google.api-key}") String apiKey,
                              @Value("${map.google.field-mask}") String fieldMask) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
        this.apiKey = apiKey;

        // apiKey 확인
        if ("FAKE_MAP_KEY".equalsIgnoreCase(this.apiKey))
            log.warn("\n\n================================\n\n" +
                    "!!!CAUTION!!!: 구글 맵의 API Key 값이 제대로 적용되지 않았습니다.\n\n" +
                    "================================\n");

        // 2. 만약 yml 설정의 fieldMask가 제대로 적용되지 않았다면, 오류가 발생하므로 강제로 다시 주입
        if (fieldMask == null || fieldMask.isEmpty()) {
            this.fieldMask = "originIndex,destinationIndex,status,condition,distanceMeters,duration";
        } else {
            this.fieldMask = fieldMask;
        }
    }

    public GoogleMapResponse fetchMatrix(GoogleMapRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        headers.set("x-goog-fieldmask", fieldMask);

        HttpEntity<GoogleMapRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<List<GoogleMapResponse.GoogleMapRouteMatrixElement>> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<GoogleMapResponse.GoogleMapRouteMatrixElement>>() {
                } // (타입 생략 가능)
        );

        log.debug("응답 바디 : {}", response.getBody());
        return new GoogleMapResponse(response.getBody());
    }
}
