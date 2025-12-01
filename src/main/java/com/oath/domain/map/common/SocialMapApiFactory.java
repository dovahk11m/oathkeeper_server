package com.oath.domain.map.common;

import com.oath.common.exception.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialMapApiFactory {

    private final List<SocialMapApiStrategy> strategies;

    private SocialMapApiStrategy findStrategy(SocialMapType type) {
        return strategies.stream()
                .filter(s -> s.match(type))
                .findFirst()
                .orElseThrow(() -> new Exception400("해당되는 소셜 Map은 존재하지 않습니다."));
    }

    // 2. 서비스가 호출할 Geocoding 전용 메서드
    public GeocodingStrategy getGeocodingStrategy(SocialMapType type) {
        SocialMapApiStrategy strategy = findStrategy(type);

        if (strategy instanceof GeocodingStrategy) {
            return (GeocodingStrategy) strategy;
        }

        throw new UnsupportedOperationException(type + "는 Geocoding을 지원하지 않습니다.");
    }

    // 3. 서비스가 호출할 ReverseGeocoding 전용 메서드
    public ReverseGeocodingStrategy getReverseGeocodingStrategy(SocialMapType type) {
        SocialMapApiStrategy strategy = findStrategy(type);
        if (strategy instanceof ReverseGeocodingStrategy) {
            return (ReverseGeocodingStrategy) strategy;
        }
        throw new UnsupportedOperationException(type + "는 ReverseGeocoding을 지원하지 않습니다.");
    }

    // 4. 서비스가 호출할 Matrix 전용 메서드
    public MatrixStrategy getMatrixStrategy(SocialMapType type) {
        SocialMapApiStrategy strategy = findStrategy(type);
        if (strategy instanceof MatrixStrategy) {
            return (MatrixStrategy) strategy;
        }

        throw new UnsupportedOperationException(type + "는 Matrix를 지원하지 않습니다.");
    }
}
