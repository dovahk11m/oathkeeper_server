package com.oath.domain.plan.event;

public enum AlarmType {
    DEPARTURE_ONE_HOUR("출발 1시간 전 알림"),
    DEPARTURE_FIVE_MINUTES("출발 5분 전 알림"),
    REAL_TIME_DEPARTURE("실시간 출발 알림"),
    NOT_DEPARTED("미출발 독촉 알림"),
    ARRIVAL("도착 인식 알림"),
    LATE("지각 알림");

    private final String description;

    AlarmType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

