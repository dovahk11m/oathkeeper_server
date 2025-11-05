package com.oath.domain.map.google;

import com.oath.domain.map.common.SocialMapApiFactory;
import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleMapService {

    private final SocialMapApiFactory socialMapApiFactory;

    public GoogleMapResponse getMatrix(GoogleMapRequest googleMapRequest) {
        SocialMapApiStrategy socialMapApiStrategy = socialMapApiFactory.find(SocialMapType.GOOGLE);
        return (GoogleMapResponse) socialMapApiStrategy.getMatrix(googleMapRequest);
    }
}
