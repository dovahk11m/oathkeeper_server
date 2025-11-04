package com.oath.domain.map.google;

import com.oath.domain.map.common.SocialMapApiStrategy;
import com.oath.domain.map.common.SocialMapType;
import org.springframework.http.ResponseEntity;

public class GoogleMapApiStrategy implements SocialMapApiStrategy {
    @Override
    public <T> ResponseEntity<?> getMatrix(T requestData) {
        return null;
    }

    @Override
    public boolean match(SocialMapType type) {
        return SocialMapType.GOOGLE.equals(type);
    }
}
