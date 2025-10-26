package com.oath.domain.members.dto;

import lombok.Data;

public class MemberRequest {

    @Data
    public static class Update{
        private String email;

    }

    @Data
    public static class PasswordUpdate {
        private String currentPassword;
        private String newPassword;

    }

    @Data
    public class FindId {
        private String email;

    }

    @Data
    public static class FindPassword {
        private String username;
        private String email;

    }
}
