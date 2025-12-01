package com.oath.domain.map.naver;

import com.oath.common.Position;
import com.oath.common.exception.Exception400;
import com.oath.domain.map.common.GeocodingStrategy;
import com.oath.domain.map.common.ReverseGeocodingStrategy;
import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
import com.oath.domain.map.naver.common.NaverMapApiClient;
import com.oath.domain.map.naver.dao.NaverMapDao;
import com.oath.domain.map.naver.dto.NaverMapGeocodingResponse;
import com.oath.domain.map.naver.dto.NaverMapReverseGeocodingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
@Slf4j
@RequiredArgsConstructor
public class NaverMapApiStrategy implements SocialMapApiStrategy,
        GeocodingStrategy<NaverMapDao.Geocoding, Position>,
        ReverseGeocodingStrategy<NaverMapDao.ReverseGeocoding, String> {

    private final NaverMapApiClient naverMapApiClient;

    @Override
    public Position toGeocoding(NaverMapDao.Geocoding geocodingData) {

        NaverMapGeocodingResponse response = naverMapApiClient.fetchGeocoding(geocodingData);
        return extractPos(response);
    }

    @Override
    public String toReverseGeocoding(NaverMapDao.ReverseGeocoding reverseGeocodingData) {

        NaverMapReverseGeocodingResponse response = naverMapApiClient.fetchReverseGeocoding(reverseGeocodingData);
        return extractAddress(response);
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
            throw new Exception400("해당 좌표의 주소가 우리나라 주소가 아닙니다.");
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
            throw new Exception400("해당 주소는 우리나라 주소가 아닙니다.");
        }

        var address = response.addresses().get(0);
        return new Position(address.x(), address.y());
    }
}
