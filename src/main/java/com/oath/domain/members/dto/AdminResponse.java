package com.oath.domain.members.dto;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class AdminResponse {

    @Data
    public static class MemberDto {
        private Long id;
        private String username;
        private String email;
        private Role role;
        private SocialType socialType;
        private Status status;
        private String socialId;
        private LocalDateTime lastLogin;
        private LocalDateTime bannedUntil;

        public MemberDto(Member member) {
            this.id = member.getId();
            this.username = member.getUsername();
            this.email = member.getEmail();
            this.role = member.getRole();
            this.socialType = member.getSocialType();
            this.status = member.getStatus();
            this.socialId = member.getSocialId();
            this.lastLogin = member.getLastLogin();
            this.bannedUntil = member.getBannedUntil();
        }
    }

    @Data
    public static class ListDto {
        private List<MemberDto> members;
    }
}
