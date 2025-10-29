package com.oath.domain.naver.map;

import com.oath.common.exception.Exception400;
import com.oath.domain.naver.map.dto.NaverMapGeocodingResponse;
import com.oath.domain.naver.map.dto.NaverMapReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.StringJoiner;

@Slf4j
@Service
public class NaverMapService {

    private final RestTemplate restTemplate;
    private final String clientSecret;

    /**
     * @param restTemplate
     * @param clientSecret
     */
    private NaverMapService(RestTemplate restTemplate, @Value("${naver.map.client-secret}") String clientSecret) {
        // 1. RestTemplate 주입
        this.restTemplate = restTemplate;

        // 2. 시크릿 키 주입
        this.clientSecret = clientSecret;
        if ("FAKE_KEY".equalsIgnoreCase(this.clientSecret))
            System.err.println("\n\n================================\n\n" +
                    "!!!CAUTION!!!: 시크릿 값이 제대로 적용되지 않았습니다.\n\n" +
                    "================================\n");
    }

    /**
     * @param latitude
     * @param longitude
     * @param clientId
     * @return String
     */
    public String getAddress(Double latitude, Double longitude, String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=" +
                longitude + "," + latitude +
                "&output=json";

        ResponseEntity<NaverMapReverseGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapReverseGeocodingResponse.class);
        return extractAddress(response.getBody());
    }

    /**
     * @param response
     * @return String
     */
    private String extractAddress(NaverMapReverseGeocodingResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new Exception400("좌표를 제대로 입력해주세요.");
        }

        var region = response.results().get(0).region();
        StringJoiner addressJoiner = new StringJoiner(" ");

        addAddressPart(addressJoiner, region.area1());
        addAddressPart(addressJoiner, region.area2());
        addAddressPart(addressJoiner, region.area3());

        return addressJoiner.toString();
    }

    /**
     * @param joiner
     * @param area
     */
    private void addAddressPart(StringJoiner joiner, NaverMapReverseGeocodingResponse.Results.Region.Area area) {
        if (area != null && area.name() != null && !area.name().isEmpty()) {
            joiner.add(area.name());
        }
    }

    /**
     * @param address
     * @param clientId
     * @return Point
     */
    public Point getPos(String address, String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

        ResponseEntity<NaverMapGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapGeocodingResponse.class);
        return extractPos(response.getBody());
    }

    /**
     * @param response
     * @return Point
     */
    private Point extractPos(NaverMapGeocodingResponse response) {
        if (response == null || response.addresses() == null || response.addresses().isEmpty()) {
            throw new Exception400("주소를 제대로 입력해주세요.");
        }

        var address = response.addresses().get(0);
        return new Point(address.x(), address.y());
    }
}
