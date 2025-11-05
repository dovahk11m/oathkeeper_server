package com.oath.domain.map.naver;

import com.oath.common.Position;
import com.oath.common.exception.Exception400;
import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
import com.oath.domain.map.naver.dao.NaverMapDao;
import com.oath.domain.map.naver.dto.NaverMapGeocodingResponse;
import com.oath.domain.map.naver.dto.NaverMapReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.StringJoiner;

@Component
@Slf4j
public class NaverMapApiStrategy implements SocialMapApiStrategy {

    private final RestTemplate restTemplate;
    private final String clientSecret;

    /**
     * @param restTemplate
     * @param clientSecret
     */
    private NaverMapApiStrategy(RestTemplate restTemplate, @Value("${map.naver.client-secret}") String clientSecret) {
        // 1. RestTemplate 주입
        this.restTemplate = restTemplate;

        // 2. 시크릿 키 주입
        this.clientSecret = clientSecret;
        if ("FAKE_KEY".equalsIgnoreCase(this.clientSecret))
            log.warn("\n\n================================\n\n" +
                    "!!!CAUTION!!!: 네이버 맵의 시크릿 값이 제대로 적용되지 않았습니다.\n\n" +
                    "================================\n");
    }

    @Override
    public <T> Position toGeocoding(T requestData) {
        if (!(requestData instanceof NaverMapDao.Geocoding geocoding)) throw new Exception400("잘못된 Request 형식입니다.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", geocoding.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + geocoding.getAddress();

        ResponseEntity<NaverMapGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapGeocodingResponse.class);
        return extractPos(response.getBody());
    }

    @Override
    public <T> String toReverseGeocoding(T requestData) {
        if (!(requestData instanceof NaverMapDao.ReverseGeocoding reverseGeocodingData))
            throw new Exception400("잘못된 Request 형식입니다.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", reverseGeocodingData.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=" +
                reverseGeocodingData.getLongitude() + "," + reverseGeocodingData.getLatitude() +
                "&output=json";

        ResponseEntity<NaverMapReverseGeocodingResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, NaverMapReverseGeocodingResponse.class);
        return extractAddress(response.getBody());
    }

    @Override
    public boolean match(SocialMapType type) {
        return SocialMapType.NAVER.equals(type);
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
     * @param response
     * @return Point
     */
    private Position extractPos(NaverMapGeocodingResponse response) {
        if (response == null || response.addresses() == null || response.addresses().isEmpty()) {
            throw new Exception400("주소를 제대로 입력해주세요.");
        }

        var address = response.addresses().get(0);
        return new Position(address.x(), address.y());
    }
}
