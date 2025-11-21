package com.oath.domain.plan;

public enum MovementStatus {
    NOT_STARTED, // 약속 참여는 수락했지만, 아직 출발하지 않은 상태
    DEPARTED,    // 출발 버튼을 눌렀거나, 위치 정보 전송이 시작된 상태
    MOVING,      // 위치 변화가 감지되며 이동 중인 상태
    STATIONARY,  // 위치 변화가 감지되지 않으며 정체 중인 상태 (아직 도착은 아님)
    ARRIVED      // 약속 장소에 도착한 상태
}
