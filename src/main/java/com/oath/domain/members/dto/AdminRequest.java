package com.oath.domain.members.dto;


import lombok.Data;


public class AdminRequest {

    @Data
    public class RoleUpdate {
        private Long memberId;
        private String role;
    }

}
