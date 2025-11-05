package com.oath.domain.members.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
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
        private String lastLogin;
        private String bannedUntil;

        public MemberDto(Member member) {
            this.id = member.getId();
            this.username = member.getUsername();
            this.email = member.getEmail();
            this.role = member.getRole();
            this.socialType = member.getSocialType();
            this.status = member.getStatus();
            this.socialId = member.getSocialId();
            this.lastLogin = member.getLastLogin() != null
                    ? member.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    : "기록 없음";
            this.bannedUntil = member.getBannedUntil() != null
                    ? member.getBannedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    : "해당 없음";
        }

        public static MemberDto from(Member member) {
            return new MemberDto(member);
        }
    }

    @Data
    public static class ListDto {
        private List<MemberDto> members;
    }

    @Data
    public static class PageDto {
        private int number; // 화면에 표시될 페이지 번호 (1, 2, 3, ...)
        private int pageIndex; // URL에 사용될 페이지 인덱스 (0, 1, 2, ...)
        private boolean isCurrent;

        public PageDto(int number, int pageIndex, boolean isCurrent) {
            this.number = number;
            this.pageIndex = pageIndex;
            this.isCurrent = isCurrent;
        }
    }

//    @Data
//    public static class MemberIndexDto {
//        private int index;
//        private Member member;
//
//        public MemberIndexDto(int index, Member member) {
//            this.index = index;
//            this.member = member;
//        }
//    }

    @Data
    public static class popularPlanTag {
        private Long tagId;
        private String tagName;
        private Long participantCount;
        private Long planCount;

        public popularPlanTag(Long tagId, String tagName, Long participantCount, Long planCount) {
            this.tagId = tagId;
            this.tagName = tagName;
            this.participantCount = participantCount;
            this.planCount = planCount;
        }
    }

    @Data
    public static class popularPlaceTag {
        private Long tagId;
        private String tagName;
        private Long placeCount;

        public popularPlaceTag(Long tagId, String tagName, Long placeCount) {
            this.tagId = tagId;
            this.tagName = tagName;
            this.placeCount = placeCount;
        }
    }

    @Data
    public static class groupListDto {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;

        public groupListDto(Group group) {
            this.id = group.getId();
            this.name = group.getName();
            this.description = group.getDescription() != null ? group.getDescription() : "설명없음";
            this.createdAt = group.getCreatedAt();
        }

        public static groupListDto from(Group group) {
            return new groupListDto(group);
        }

    }


}
