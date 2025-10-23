package com.oath.domain.plan.request;


import com.oath.domain.plan.Plan.Option;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class PlanRequest {

    // 요청 DTO들만 남깁니다.
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreatePlanRequest {
        public Long creatorMemberId;
        public String title;
        public String planDatetime;
        public String status;
        public Long lateFineAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdatePlanRequest {
        public String title;
        public String planDatetime;
        public String status;

        // 새로 추가된 요청 필드들
        public List<String> negative;
        public List<String> positive;
        public Option option;
        public List<String> tags; // 변경: 엔티티 대신 태그명 리스트로 수신
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParticipantAddRequest {
        public Long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParticipantStatusRequest {
        public String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TimeRecordRequest {
        public String time;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SuggestDepartureRequest {
        public Integer expectedTravelTimeMinutes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ConfirmPlaceRequest {
        public String placeName;
        public Double latitude;
        public Double longitude;
    }

}
