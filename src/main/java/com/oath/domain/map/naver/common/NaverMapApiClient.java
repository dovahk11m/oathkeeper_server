package com.oath.domain.map.naver.common;

import com.oath.domain.map.naver.dao.NaverMapDao;
import com.oath.domain.map.naver.dto.NaverMapGeocodingResponse;
import com.oath.domain.map.naver.dto.NaverMapReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class NaverMapApiClient {
    private final RestTemplate restTemplate;
    private final String geocodingEndpoint;
    private final String reverseGeocodingEndpoint;
    private final String clientSecret;

    /**
     * @param restTemplate
     * @param clientSecret
     */
    public NaverMapApiClient(RestTemplate restTemplate,
                             @Value("${map.naver.geocoding-endpoint}") String geocodingEndpoint,
                             @Value("${map.naver.reverse-geocoding-endpoint}") String reverseGeocodingEndpoint,
                             @Value("${map.naver.client-secret}") String clientSecret) {
        // 1. RestTemplate 주입
        this.restTemplate = restTemplate;

        // 2. url 주입
        this.geocodingEndpoint = geocodingEndpoint;
        this.reverseGeocodingEndpoint = reverseGeocodingEndpoint;

        // 3. 시크릿 키 주입
        this.clientSecret = clientSecret;
        if ("FAKE_KEY".equalsIgnoreCase(this.clientSecret))
            log.warn("\n\n================================\n\n" +
                    "!!!CAUTION!!!: 네이버 맵의 시크릿 값이 제대로 적용되지 않았습니다.\n\n" +
                    "================================\n");
    }

    public NaverMapGeocodingResponse fetchGeocoding(NaverMapDao.Geocoding geocodingData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", geocodingData.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = geocodingEndpoint + geocodingData.getAddress();

        ResponseEntity<NaverMapGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapGeocodingResponse.class);
        return response.getBody();
    }

    public NaverMapReverseGeocodingResponse fetchReverseGeocoding(NaverMapDao.ReverseGeocoding reverseGeocodingData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", reverseGeocodingData.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = reverseGeocodingEndpoint +
                reverseGeocodingData.getLongitude() + "," + reverseGeocodingData.getLatitude() +
                "&output=json";

        ResponseEntity<NaverMapReverseGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapReverseGeocodingResponse.class);
        return response.getBody();
    }
}
