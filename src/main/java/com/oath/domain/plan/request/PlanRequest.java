package com.oath.domain.plan.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PlanRequest {

    // 요청(Request) DTO들만 남깁니다.
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
