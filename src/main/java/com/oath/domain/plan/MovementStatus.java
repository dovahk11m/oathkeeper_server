package com.oath.domain.plan;

public enum MovementStatus {
    HOME,        // 아직 출발하지 않고 집에 있음
    DEPARTED,    // 출발 버튼을 눌렀거나, 위치 정보 전송이 시작
    MOVING,      // 위치 변화가 감지되며 이동 중
    STATIONARY,  // 위치 변화가 감지되지 않으며 정체 중
    ARRIVED      // 약속 장소에 도착
}
