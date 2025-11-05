package com.oath.domain.map.common;

public interface GeocodingStrategy<Req, Res> {
    default Res toGeocoding(Req requestData) {
        throw new UnsupportedOperationException("이 API에서 지원하지 않는 기능입니다");
    }
}
