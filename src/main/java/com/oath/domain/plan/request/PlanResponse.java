package com.oath.domain.plan.request;

import com.oath.domain.plan.domain.Plan;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
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

    // AI 요약 보고서 응답을 위한 Summary DTO
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Summary {
        private Long id;
        private String title;
        private String summary;

        public Summary(Long id, String title, String summary) {
            this.id = id;
            this.title = title;
            this.summary = summary;
        }
    }

    // 그룹 내 약속 목록 조회를 위한 SimplePlan DTO
    @Getter
    @NoArgsConstructor
    @ToString
    public static class SimplePlan {
        private Long planId;
        private String title;
        private LocalDateTime planDatetime;

        public SimplePlan(Plan plan) {
            this.planId = plan.getId();
            this.title = plan.getTitle();
            this.planDatetime = plan.getPlanDatetime();
        }

        public static SimplePlan of(Plan plan) {
            return new SimplePlan(plan);
        }
    }
}
