package com.oath.domain.members.dto;


import lombok.Data;


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


}
