package com.oath.domain.map.google;

import com.oath.common.exception.Exception400;
import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
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
public class GoogleMapApiStrategy implements SocialMapApiStrategy {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String fieldMask;

    private GoogleMapApiStrategy(RestTemplate restTemplate,
                                 @Value("${map.google.api-key}") String apiKey,
                                 @Value("${map.google.field-mask}") String fieldMask) {
        this.restTemplate = restTemplate;
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

    @Override
    public <T> GoogleMapResponse getMatrix(T requestData) {
        if (!(requestData instanceof GoogleMapRequest googleMapRequest))
            throw new Exception400("잘못된 Request 형식입니다.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        headers.set("x-goog-fieldmask", fieldMask);

        HttpEntity<GoogleMapRequest> entity = new HttpEntity<>(googleMapRequest, headers);

        String url = "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix";

        ResponseEntity<List<GoogleMapResponse.GoogleMapRouteMatrixElement>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<GoogleMapResponse.GoogleMapRouteMatrixElement>>() {
                }
        );

        log.debug("응답 바디 : {}", response.getBody());
        return new GoogleMapResponse(response.getBody());
    }

    @Override
    public boolean match(SocialMapType type) {
        return SocialMapType.GOOGLE.equals(type);
    }
}
