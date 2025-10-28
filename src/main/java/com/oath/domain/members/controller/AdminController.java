package com.oath.domain.members.controller;

import com.oath.common.CommonResponse;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/member-list")
    public ResponseEntity<?> memberList() {
        AdminResponse.ListDto list = adminService.list();
        return ResponseEntity.ok(CommonResponse.success(list, "회원 조회 성공"));
    }

    @PostMapping("/ban-member")
    public void banMember(Member member, int days){
        adminService.banMember(member, days);
    }

}
