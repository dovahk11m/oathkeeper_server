package com.oath.domain.members.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateDto {
    @Schema(description = "사용자 이름", example = "김오스")
    private String username;

    @Schema(description = "사용자 이메일 (로그인 시 ID로 사용)", example = "test@oath.com")
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    private String password;

    @Schema(description = "동의한 약관 ID 목록", example = "[1, 2, 3, 4]")
    private Set<Long> agreedTermIds;
}
