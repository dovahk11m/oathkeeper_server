package com.oath.domain.map.naver.dao;

import lombok.Builder;
import lombok.Data;

public class NaverMapDao {

    @Data
    @Builder
    public static class Geocoding {
        private String address;
        private String clientId;
    }

    @Data
    @Builder
    public static class ReverseGeocoding {
        private double latitude;
        private double longitude;
        private String clientId;
    }
}
