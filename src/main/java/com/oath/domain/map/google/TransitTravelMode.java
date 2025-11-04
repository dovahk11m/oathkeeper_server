package com.oath.domain.map.google;

public enum TransitTravelMode {
    /**
     * 지정된 대중교통 이동 수단이 없는 경우
     */
    TRANSIT_TRAVEL_MODE_UNSPECIFIED,

    /**
     * 버스로 이동하는 경우(기본값)
     */
    BUS,

    /**
     * 지하철로 이동하는 경우(기본값)
     */
    SUBWAY,

    /**
     * 기차로 이동하는 경우
     */
    TRAIN,

    /**
     * 경전철 또는 전차로 이동하는 경우
     */
    LIGHT_RAIL,

    /**
     * 지하철, 기차, 경전철 또는 전차를 모두 아우르는 설정
     */
    RAIL
}
