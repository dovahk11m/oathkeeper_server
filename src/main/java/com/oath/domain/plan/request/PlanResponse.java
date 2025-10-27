package com.oath.domain.plan.request;

import org.springframework.data.geo.Point;
import com.oath.domain.plan.domain.Plan;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PlanResponse {
    @Getter
    @NoArgsConstructor
    @ToString(exclude = "participants")
    public static class CreatePlan {
        private String date;
        private String time;
        private String title;
        private String location;
        private Double placeLatitude;
        private Double placeLongitude;
        private List<ParticipantResponse> participants;

        public CreatePlan(Plan plan) {
            if (plan != null && plan.getPlanDatetime() != null) {
                this.date = plan.getPlanDatetime().toLocalDate().toString();
                this.time = plan.getPlanDatetime().toLocalTime().toString();
            }
            this.title = plan != null ? plan.getTitle() : null;
            this.location = plan != null ? plan.getPlaceName() : null;
            Point lp = plan != null ? plan.getPlaceLocation() : null;
            if (lp != null) {
                this.placeLatitude = lp.getY();
                this.placeLongitude = lp.getX();
            }
            this.participants = plan != null ? plan.getParticipants().stream().map(pm -> ParticipantResponse.of(pm)).collect(Collectors.toList()) : null;
        }

        public static CreatePlan of(Plan plan) {
            return new CreatePlan(plan);
        }
    }

    @Getter
    @NoArgsConstructor
    @ToString(exclude = "participants")
    public static class UpdatePlan {
        private String date;
        private String time;
        private String title;
        private String location;
        private Double placeLatitude;
        private Double placeLongitude;
        private List<ParticipantResponse> participants;

        public UpdatePlan(Plan plan) {
            if (plan != null && plan.getPlanDatetime() != null) {
                this.date = plan.getPlanDatetime().toLocalDate().toString();
                this.time = plan.getPlanDatetime().toLocalTime().toString();
            }
            this.title = plan != null ? plan.getTitle() : null;
            this.location = plan != null ? plan.getPlaceName() : null;
            Point lp = plan != null ? plan.getPlaceLocation() : null;
            if (lp != null) {
                this.placeLatitude = lp.getY();
                this.placeLongitude = lp.getX();
            }
            this.participants = plan != null ? plan.getParticipants().stream().map(pm -> ParticipantResponse.of(pm)).collect(Collectors.toList()) : null;
        }


        public static UpdatePlan of(Plan plan) {
            return new UpdatePlan(plan);
        }
    }

}
