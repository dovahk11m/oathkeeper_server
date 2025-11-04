package com.oath.domain.members.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateDto {
    private String username;
    private String email;
    private String password;
    private Set<Long> agreedTermIds;
}
