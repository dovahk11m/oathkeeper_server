package com.oath.domain.members.dto;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.Status;
import lombok.Data;

public class MemberResponse {

    @Data
    public static class DTO {
        private Long id;
        private String username;
        private String email;

        public DTO(Member member) {
            this.id = member.getId();
            this.username = member.getUsername();
            this.email = member.getEmail();
        }
    }

    @Data
    public static class Login {
        private String token;
        private MemberInfo member;

        public Login(String token, Member member) {
            this.token = token;
            this.member = new MemberInfo(member);
        }

        @Data
        public static class MemberInfo {
            private Long id;
            private String username;
            private String email;
            private Role role;
            private Status status;

            public MemberInfo(Member member) {
                this.id = member.getId();
                this.username = member.getUsername();
                this.email = member.getEmail();
                this.role = member.getRole();
                this.status = member.getStatus();
            }
        }
    }
}
