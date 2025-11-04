package com.oath.domain.map.naver;

import com.oath.common.Position;
import com.oath.domain.map.common.SocialMapApiFactory;
import com.oath.domain.map.common.SocialMapApiStrategy;
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
        SocialMapApiStrategy socialMapApiStrategy = socialMapApiFactory.find(SocialMapType.NAVER);
        return (String) socialMapApiStrategy.toReverseGeocoding(NaverMapDao.ReverseGeocoding
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
        SocialMapApiStrategy socialMapApiStrategy = socialMapApiFactory.find(SocialMapType.NAVER);
        return (Position) socialMapApiStrategy.toGeocoding(NaverMapDao.Geocoding
                .builder()
                .address(address)
                .clientId(clientId)
                .build());
    }
}
