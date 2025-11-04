package com.oath.domain.members.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class MemberRequest {

    @Data
    public static class Update{
        private String username;
        private String profileImageUrl;
        private String defaultAddress;
    }

    @Data
    public static class PasswordUpdate {

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;

        @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 16, message = "새로운 비밀번호는 8자 이상 16자 이하여야 합니다.")
        private String newPassword;

    }

    @Data
    public static class FindId {
        private String email;

    }

    @Data
    public static class FindPassword {
        private String email;

    }

    @Data
    public static class CheckPassword {
        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }
}
