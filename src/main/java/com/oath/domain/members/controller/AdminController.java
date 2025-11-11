package com.oath.domain.members.controller;


import com.oath.domain.members.dto.ActiveChartDto;
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
import java.time.LocalDateTime;
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
    public String banMember(@RequestParam Long id, @RequestParam int days, @RequestParam int page){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        adminService.banMember(member, days);
        return "redirect:/api/admin/members?page=" + page;
    }

//    @GetMapping("/members")
//    public Page<AdminResponse.MemberDto> getMembers(Pageable pagable) {
//        Page<AdminResponse.MemberDto> members = adminService.getMembers(pagable);
//        return members;
//    }

    @PostMapping("/update-role")
    public String updateRole(@RequestParam Long id, @RequestParam String role, @RequestParam int page) {
            adminService.updateRole(id, role);
            return "redirect:/api/admin/members?page=" + page;
    }

    @GetMapping("/members")
    public String getMembers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.MemberDto> memberPage = adminService.getMembers(pageable);
                //memberRepository.findAll(pageable).map(member -> new AdminResponse.MemberDto(member));

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);

        List<AdminResponse.PageDto> pages = IntStream.range(0, memberPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);

        return "member";
    }

    @GetMapping("/popular-plan-tag")
    @ResponseBody
    public List<AdminResponse.popularPlanTag> getPopularPlanTag(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);

        List<AdminResponse.popularPlanTag> popularPlanTags = adminService.getPopularPlanTag(startDateTime, endDateTime);

        return popularPlanTags;
    }

    @GetMapping("/group-list")
    public String getGroupList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.groupListDto> groupPage = adminService.getGroupList(pageable);

        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("currentPage", page);

        List<AdminResponse.PageDto> pages = IntStream.range(0, groupPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);
        return "chatGrouptest";
    }

    @GetMapping("/chat-list/{groupId}")
    public String getChat(@PathVariable Long groupId, Model model) {
        List<AdminResponse.ChatDto> chats = adminService.chatList(groupId);
        List<AdminResponse.ChatMemberDto> chatMembers = adminService.chatMember(groupId);
        List<AdminResponse.PlanDto> plans = adminService.getPlanList(groupId);
        model.addAttribute("plans", plans);
        model.addAttribute("chats", chats);
        model.addAttribute("chatMembers", chatMembers);
        return "chat";
    }

    @GetMapping("/chat-list/{groupId}/{memberId}")
    @ResponseBody
    public List<AdminResponse.ChatDto> getChatByMember( @PathVariable Long groupId, @PathVariable Long memberId, Model model) {
        List<AdminResponse.ChatDto> chats = adminService.chatListByMember(groupId, memberId);

        return chats;
    }

    @GetMapping("/active-chart")
    @ResponseBody
    public List<ActiveChartDto> getActiveChart() {
        List<ActiveChartDto> activeChart = adminService.activeCharts();
        activeChart.add(new ActiveChartDto(9,0,12L));
        activeChart.add(new ActiveChartDto(10,0,7L));
        activeChart.add(new ActiveChartDto(14,1,10L));
        return activeChart;
    }

    @GetMapping("/plan-tag-pie")
    @ResponseBody
    public List<AdminResponse.PlanTagPie> PlanTagPie(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);

        List<AdminResponse.PlanTagPie> PlanTagsPie = adminService.PlanTagPie(startDateTime, endDateTime);

        return PlanTagsPie;
    }


}
