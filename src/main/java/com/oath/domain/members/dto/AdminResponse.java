package com.oath.domain.members.dto;

import com.oath.domain.chats.Chat;
import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.tag.Tag;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
        private Boolean isAdmin;

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
            this.isAdmin = member.getRole() == Role.ADMIN;
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
    public static class PlanTagPie {
        private String tagName;
        private Long planCount;

        public PlanTagPie(String tagName, Long planCount) {
            this.tagName = tagName;
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
        private String createdAt;

        public groupListDto(Group group) {
            this.id = group.getId();
            this.name = group.getName();
            this.description = group.getDescription() != null ? group.getDescription() : "설명없음";
            this.createdAt = group.getCreatedAt() != null ? group.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "기록 없음";
        }

        public static groupListDto from(Group group) {
            return new groupListDto(group);
        }
    }

    @Data
    public static class ChatDto {
        private Long id;
        private Long groupId;
        private Long memberId;
        private String senderName;
        private String content;
        private String sentAt;

        public ChatDto(Chat chat) {
            this.id = chat.getId();
            this.groupId = chat.getGroup().getId();
            this.memberId = chat.getSender().getId();
            this.senderName = chat.getSender().getUsername();
            this.content = chat.getContent();
            this.sentAt = chat.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        public static ChatDto from(Chat chat) {return new ChatDto(chat);}
    }

    @Data
    public static class ChatMemberDto{
        private Long id;
        private String username;
        private String profileImageUrl;
        private Long groupId;

        public ChatMemberDto(Long id, String username, String profileImageUrl, Long groupId) {
            this.id = id;
            this.username = username;
            this.profileImageUrl = profileImageUrl;
            this.groupId = groupId;
        }
    }

    @Data
    public static class PlanDto {
        private Long planId;
        private Long creatorId;
        private String planDatetime;
        private String placeName;
        private String title;
        private List<String> tags;
        private List<String> profileImageUrl;


        public PlanDto(Long planId, Long creatorId, LocalDateTime planDatetime, String placeName, String title, List<String> tags, List<String> profileImageUrl) {
            this.planId = planId;
            this.creatorId = creatorId;
            this.planDatetime = planDatetime != null ? planDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "기록 없음";
            this.placeName = placeName != null ? placeName : "기록 없음";
            this.title = title;
            this.tags = tags != null ? tags : Collections.emptyList();
            this.profileImageUrl = profileImageUrl;
        }
    }

    @Data
    public static class DailyTagCount {
        private List<String> dates;
        private List<DailyTag> dailyTags;

        public DailyTagCount(List<String> dates, List<DailyTag> dailyTags) {
            this.dates = dates;
            this.dailyTags = dailyTags;
        }

        @Data
        public static class DailyTag{
            private String tagName;
            private List<Long> count;

            public DailyTag(String tagName, List<Long> count) {
                this.tagName = tagName;
                this.count = count;
            }
        }
    }

    public interface MonthlyCount {
        String getMonth();
        Long getPlanCount();
        Long getParticipantCount();
    }

    @Data
    public static class activeCount {
        private String username;
        private Long count;

        public activeCount(String username, Long count) {
            this.username = username;
            this.count = count;
        }
    }

    @Data
    public static class placeList {
        private Long id;
        private String name;
        private String description;

        public placeList(Place place) {
            this.id = place.getId();
            this.name = place.getName();
            this.description = place.getDescription();
        }
    }

    @Data
    public static class tagList {
        private String name;

        public tagList(Tag tag) {
            this.name = tag.getName();
        }
    }

    @Data
    public static class PlaceTag {
        private Long id;
        private String name;
        private List<TagDto> tags;

        public PlaceTag(Long id, String name, List<TagDto> tags) {
            this.id = id;
            this.name = name;
            this.tags = tags;
        }
    }

    @Data
    public static class TagDto {
        private Long id;
        private String name;

        public TagDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Data
    public static class AddedTagDto {
        private Long placeId;
        private Long id;
        private String name;

        public AddedTagDto(Long placeId, Long id, String name) {
            this.placeId = placeId;
            this.id = id;
            this.name = name;
        }
    }

    @Data
    public static class GroupList {
        private Long id;
        private String name;
        private String createdAt;
        private Long memberCount;
        private String sentAt;


        public GroupList(Long id, String name, LocalDateTime createdAt, Long memberCount, LocalDateTime sentAt) {
            this.id = id;
            this.name = name;
            this.createdAt = createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "기록 없음";
            this.memberCount = memberCount;
            this.sentAt = sentAt != null ? sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "기록 없음";

        }
    }


}
