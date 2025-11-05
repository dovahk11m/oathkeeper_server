package com.oath.domain.map.google;

import com.oath.domain.map.common.MatrixStrategy;
import com.oath.domain.map.common.SocialMapApiFactory;
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
        MatrixStrategy rawStrategy = socialMapApiFactory.getMatrixStrategy(SocialMapType.GOOGLE);

        @SuppressWarnings("unchecked")
        MatrixStrategy<GoogleMapRequest, GoogleMapResponse> matrixStrategy =
                (MatrixStrategy<GoogleMapRequest, GoogleMapResponse>) rawStrategy;

        return matrixStrategy.getMatrix(googleMapRequest);
    }
}
