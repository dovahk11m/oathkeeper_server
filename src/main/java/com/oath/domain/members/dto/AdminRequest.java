package com.oath.domain.members.dto;


import lombok.Data;

import java.util.List;


public class AdminRequest {

    @Data
    public class RoleUpdate {
        private Long memberId;
        private String role;
    }

    @Data
    public static class ChatMessage {

        private String message;

        // 생성자, getter, setter
        public ChatMessage(String message) {

            this.message = message;
        }
    }

    @Data
    public static class updateDescription {
        private String description;
    }

    @Data
    public static class TagRequest {
        private String name;
    }

    @Data
    public static class PlaceTag {
        private List<Long> placeIds;
        private List<String> tags;

    }


}
