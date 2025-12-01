package com.oath.domain.members.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberLoginDto {
    @Schema(description = "로그인할 이메일 주소", example = "user1@test.com")
    private String email;

    @Schema(description = "비밀번호", example = "1234")
    private String password;
}
