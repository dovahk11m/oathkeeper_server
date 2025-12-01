package com.oath.domain.groups.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GroupMembersAddRequest {
    private List<String> memberEmails;
}