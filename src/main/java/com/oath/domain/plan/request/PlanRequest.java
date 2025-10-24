package com.oath.domain.plan.request;


import com.oath.domain.plan.Plan.Option;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class PlanRequest {


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


        public List<String> negative;
        public List<String> positive;
        public Option option;
        public List<String> tags;
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
