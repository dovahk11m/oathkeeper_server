package com.oath.domain.plan.request;


import com.oath.domain.plan.Plan;
import com.oath.domain.plan.PlanMember;

import java.util.List;

public class PlanRequest {

    public static class CreatePlan {
        private String date; // 약속 날짜
        private String time; // 약속 시간
        private String keyword; // 약속 키워드
        private String location; // 약속 장소
        private List<PlanMember> members; // 약속 참여자 목록

        public CreatePlan(Plan plan) {
            this.date = plan.getMeetingTime().toLocalDate().toString();
            this.time = plan.getMeetingTime().toLocalTime().toString();
            this.location = plan.getPlaceName();
            this.members = plan.getMembers();
        }

        public CreatePlan() {}

    }

    public static class UpdatePlan {
        private String date; // 약속 날짜
        private String time; // 약속 시간
        private String keyword; // 약속 키워드
        private String location; // 약속 장소
        private List<PlanMember> members; // 약속 참여자 목록

        public UpdatePlan(Plan plan) {
            this.date = plan.getMeetingTime().toLocalDate().toString();
            this.time = plan.getMeetingTime().toLocalTime().toString();
            this.location = plan.getPlaceName();
            this.members = plan.getMembers();
        }

        public UpdatePlan() {

        }


    }

}
