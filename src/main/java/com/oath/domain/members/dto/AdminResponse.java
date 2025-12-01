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

import java.math.BigDecimal;
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

    public interface MonthlyCount {
        String getMonth();
        Long getPlanCount();
        Long getParticipantCount();
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

    @Data
    public static class PlanCount {
        private String monthweek;
        private BigDecimal count;

        public PlanCount(String monthweek, BigDecimal count) {
            this.monthweek = monthweek;
            this.count = count;
        }
    }

    @Data
    public static class ParticipantCount {
        private String monthweek;
        private BigDecimal count;

        public ParticipantCount(String monthweek, BigDecimal count) {
            this.monthweek = monthweek;
            this.count = count;
        }
    }

    @Data
    public static class ChatListDto {
        private Long groupId;
        private Long chatCount;
        private Long memberCount;
        private String lastSentAt;

        public ChatListDto(Long groupId, Long chatCount, Long memberCount, LocalDateTime lastSentAt) {
            this.groupId = groupId;
            this.chatCount = chatCount;
            this.memberCount = memberCount;
            this.lastSentAt = lastSentAt != null ? lastSentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "기록 없음";
        }
    }

}
