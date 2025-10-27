package com.oath.domain.members.dto;

import com.oath.domain.members.domain.Member;
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
}
