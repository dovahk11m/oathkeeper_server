package com.oath.domain.map.google;

public enum RouteTravelMode {
    /**
     * 이동수단이 지정되지 않은 경우
     */
    TRAVEL_MODE_UNSPECIFIED,

    /**
     * 승용차로 이동하는 경우
     */
    DRIVE,

    /**
     * 자전거로 이동하는 경우(베타 버전)
     */
    BICYCLE,

    /**
     * 걸어서 이동하는 경우(베타 버전)
     */
    WALK,

    /**
     * 이륜차(BICYCLE 이동 모드와 다름, 베타버전)
     *
     * @예시 오토바이 같은 전동장치
     */
    TWO_WHEELER,

    /**
     * 대중교통 경로로 이동하는 경우
     */
    TRANSIT
}
