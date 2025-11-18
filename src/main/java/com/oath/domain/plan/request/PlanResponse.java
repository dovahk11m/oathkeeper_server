package com.oath.domain.plan.request;

import com.oath.domain.plan.domain.Plan;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PlanResponse {
    @Getter
    @NoArgsConstructor
    @ToString(exclude = "participants")
    public static class CreatePlan {
        private Long id;
        private String date;
        private String time;
        private String title;
        private String location;
        private Double placeLatitude;
        private Double placeLongitude;
        private String status;
        private List<ParticipantResponse> participants;

        public CreatePlan(Plan plan) {
            if (plan == null) return;

            this.id = plan.getId();
            this.title = plan.getTitle();
            this.location = plan.getPlaceName();

            if (plan.getPlanDatetime() != null) {
                this.date = plan.getPlanDatetime().toLocalDate().toString();
                this.time = plan.getPlanDatetime().toLocalTime().toString();
            }

            Point lp = plan.getPlaceLocation();
            if (lp != null) {
                this.placeLatitude = lp.getY();
                this.placeLongitude = lp.getX();
            }

            this.status = plan.getStatus().name(); // status 값 할당

            this.participants = plan.getParticipants().stream()
                    .map(ParticipantResponse::of)
                    .collect(Collectors.toList());
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
            if (plan == null) return;

            this.title = plan.getTitle();
            this.location = plan.getPlaceName();

            if (plan.getPlanDatetime() != null) {
                this.date = plan.getPlanDatetime().toLocalDate().toString();
                this.time = plan.getPlanDatetime().toLocalTime().toString();
            }

            Point lp = plan.getPlaceLocation();
            if (lp != null) {
                this.placeLatitude = lp.getY();
                this.placeLongitude = lp.getX();
            }

            this.participants = plan.getParticipants().stream()
                    .map(ParticipantResponse::of)
                    .collect(Collectors.toList());
        }


        public static UpdatePlan of(Plan plan) {
            return new UpdatePlan(plan);
        }
    }

}
