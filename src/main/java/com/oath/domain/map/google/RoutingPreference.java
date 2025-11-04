package com.oath.domain.map.google;

public enum RoutingPreference {

    /**
     * 라우팅 환경설정이 지정되지 않은 경우
     */
    ROUTING_PREFERENCE_UNSPECIFIED,

    /**
     * 실시간 교통상황을 고려하지 않고 경로를 계산하고 싶은 경우
     */
    TRAFFIC_UNAWARE,

    /**
     * 실시간 교통상황을 고려하여 경로를 계산하고 싶은 경우
     */
    TRAFFIC_AWARE,

    /**
     * 대부분의 성능 최적화를 적용하지 않고 실시간 교통상황을 고려하여 경로를 계산하고 싶은 경우
     */
    TRAFFIC_AWARE_OPTIMAL,
}
