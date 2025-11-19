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
        activeChart.add(new ActiveChartDto(0, 1, 5L));
        activeChart.add(new ActiveChartDto(1, 1, 3L));
        activeChart.add(new ActiveChartDto(2, 2, 7L));
        activeChart.add(new ActiveChartDto(3, 2, 2L));
        activeChart.add(new ActiveChartDto(4, 3, 6L));
        activeChart.add(new ActiveChartDto(5, 3, 8L));
        activeChart.add(new ActiveChartDto(6, 4, 12L));
        activeChart.add(new ActiveChartDto(7, 4, 20L));
        activeChart.add(new ActiveChartDto(8, 5, 15L));
        activeChart.add(new ActiveChartDto(9, 5, 18L));
        activeChart.add(new ActiveChartDto(10, 6, 10L));
        activeChart.add(new ActiveChartDto(11, 6, 9L));
        activeChart.add(new ActiveChartDto(12, 7, 14L));
        activeChart.add(new ActiveChartDto(13, 7, 16L));
        activeChart.add(new ActiveChartDto(14, 1, 10L));
        activeChart.add(new ActiveChartDto(15, 2, 8L));
        activeChart.add(new ActiveChartDto(16, 3, 6L));
        activeChart.add(new ActiveChartDto(17, 4, 4L));
        activeChart.add(new ActiveChartDto(18, 5, 7L));
        activeChart.add(new ActiveChartDto(19, 6, 11L));
        activeChart.add(new ActiveChartDto(20, 7, 13L));
        activeChart.add(new ActiveChartDto(21, 1, 9L));
        activeChart.add(new ActiveChartDto(22, 2, 5L));
        activeChart.add(new ActiveChartDto(23, 3, 3L));
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

    @GetMapping("/daily-tag-count")
    @ResponseBody
    public AdminResponse.DailyTagCount buildDailyTagCount(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);
        AdminResponse.DailyTagCount dailyTagCount = adminService.buildDailyTagCount(startDateTime, endDateTime);

        AdminResponse.DailyTagCount tags = createDummyDailyTagCount();

        return tags;
    }

    public static AdminResponse.DailyTagCount createDummyDailyTagCount() {
        // 날짜 리스트 예시 (1주일)
        List<String> dates = List.of(
                "2025-11-11",
                "2025-11-12",
                "2025-11-13",
                "2025-11-14",
                "2025-11-15",
                "2025-11-16",
                "2025-11-17"
        );

        // 태그별 카운트 예시 (날짜 순서대로)
        List<AdminResponse.DailyTagCount.DailyTag> dailyTags = List.of(
                new AdminResponse.DailyTagCount.DailyTag(
                        "Java",
                        List.of(5L, 3L, 4L, 6L, 5L, 7L, 8L)
                ),
                new AdminResponse.DailyTagCount.DailyTag(
                        "Spring",
                        List.of(2L, 4L, 3L, 5L, 4L, 6L, 5L)
                ),
                new AdminResponse.DailyTagCount.DailyTag(
                        "React",
                        List.of(0L, 1L, 2L, 1L, 3L, 2L, 4L)
                )
        );

        return new AdminResponse.DailyTagCount(dates, dailyTags);
    }

    @GetMapping("/bar-chart")
    @ResponseBody
    public List<AdminResponse.barChart> getBarChart() {
        List<AdminResponse.barChart> chart = adminService.getBarChart();
        return chart;
    }

    @GetMapping("/active-count")
    @ResponseBody
    public List<AdminResponse.activeCount> getActiveCount() {
        List<AdminResponse.activeCount> activeCount = adminService.getActiveCount();
        return activeCount;
    }


}
