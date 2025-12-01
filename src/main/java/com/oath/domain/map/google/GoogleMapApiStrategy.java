package com.oath.domain.map.google;

import com.oath.domain.map.common.MatrixStrategy;
import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
import com.oath.domain.map.google.common.GoogleMapApiClient;
import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleMapApiStrategy implements SocialMapApiStrategy, MatrixStrategy<GoogleMapRequest, GoogleMapResponse> {

    private final GoogleMapApiClient googleMapApiClient;

    @Override
    public GoogleMapResponse getMatrix(GoogleMapRequest googleMapRequest) {
        return googleMapApiClient.fetchMatrix(googleMapRequest);
    }

    @Override
    public boolean match(SocialMapType type) {
        return SocialMapType.GOOGLE.equals(type);
    }
}
