package com.oath.domain.members.controller;


import io.sentry.Sentry;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.oath.common.CommonResponse;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminRequest;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.members.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final MemberRepository memberRepository;


//    @GetMapping("/member-list")
//    public ResponseEntity<?> memberList() {
//        AdminResponse.ListDto list = adminService.list();
//        return ResponseEntity.ok(CommonResponse.success(list, "회원 조회 성공"));
//    }

    @PostMapping("/ban-member")
    public String banMember(Long id, int days){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        adminService.banMember(member, days);
        return "redirect:/api/admin/members";
    }

//    @GetMapping("/members")
//    public Page<AdminResponse.MemberDto> getMembers(Pageable pagable) {
//        Page<AdminResponse.MemberDto> members = adminService.getMembers(pagable);
//        return members;
//    }

    @PostMapping("/update-role")
    public String updateRole(@RequestParam Long id, @RequestParam String role) {
            adminService.updateRole(id, role);
            return "redirect:/api/admin/members";
    }

    @GetMapping("/members")
    public String getMembers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.MemberDto> memberPage = adminService.getMembers(pageable);
                //memberRepository.findAll(pageable).map(member -> new AdminResponse.MemberDto(member));

        model.addAttribute("members", memberPage.getContent());

        List<AdminResponse.PageDto> pages = IntStream.range(0, memberPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);

        return "member";
    }

    @GetMapping("/popular-plan-tag")
    public String getPopularPlanTag(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AdminResponse.popularPlanTag> popularPlanTags = adminService.getPopularPlanTag(startDate, endDate);

        return "popularPlanTag";
    }

}
