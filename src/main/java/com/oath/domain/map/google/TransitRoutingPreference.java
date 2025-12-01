package com.oath.domain.map.google;

public enum TransitRoutingPreference {

    /**
     * 선호사항이 지정되지 않은 경우
     */
    TRANSIT_ROUTING_PREFERENCE_UNSPECIFIED,

    /**
     * 경로에 걷기를 최대한 제한하고 싶은 경우
     */
    LESS_WALKING,

    /**
     * 경로에 환승을 최대한 제한하고 싶은 경우
     */
    FEWER_TRANSFERS
}
