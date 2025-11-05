package com.oath.domain.map.naver;

import com.oath.common.Position;
import com.oath.domain.map.common.GeocodingStrategy;
import com.oath.domain.map.common.ReverseGeocodingStrategy;
import com.oath.domain.map.common.SocialMapApiFactory;
import com.oath.domain.map.common.SocialMapType;
import com.oath.domain.map.naver.dao.NaverMapDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverMapService {

    private final SocialMapApiFactory socialMapApiFactory;

    /**
     * @param latitude
     * @param longitude
     * @param clientId
     * @return String
     */
    public String getAddress(Double latitude, Double longitude, String clientId) {
        ReverseGeocodingStrategy rawStrategy = socialMapApiFactory.getReverseGeocodingStrategy(SocialMapType.NAVER);

        @SuppressWarnings("unchecked")
        ReverseGeocodingStrategy<NaverMapDao.ReverseGeocoding, String> reverseGeocodingStrategy =
                (ReverseGeocodingStrategy<NaverMapDao.ReverseGeocoding, String>) rawStrategy;

        return reverseGeocodingStrategy.toReverseGeocoding(NaverMapDao.ReverseGeocoding
                .builder()
                .latitude(latitude)
                .longitude(longitude)
                .clientId(clientId)
                .build());
    }

    /**
     * @param address
     * @param clientId
     * @return Position
     */
    public Position getPos(String address, String clientId) {
        GeocodingStrategy rawStrategy = socialMapApiFactory.getGeocodingStrategy(SocialMapType.NAVER);

        @SuppressWarnings("unchecked")
        GeocodingStrategy<NaverMapDao.Geocoding, Position> geocodingStrategy =
                (GeocodingStrategy<NaverMapDao.Geocoding, Position>) rawStrategy;

        return geocodingStrategy.toGeocoding(NaverMapDao.Geocoding
                .builder()
                .address(address)
                .clientId(clientId)
                .build());
    }
}
