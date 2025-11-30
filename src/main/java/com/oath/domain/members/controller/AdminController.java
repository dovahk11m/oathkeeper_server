package com.oath.domain.members.controller;


import com.oath.common.JwtTokenProvider;
import com.oath.common.exception.Exception401;
import com.oath.domain.chatEntity.ChatEntity;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.dto.ActiveChartDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.service.MemberService;
import com.oath.domain.members.service.SummaryService;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRequestDto;
import com.oath.domain.place_tag_plan.place.PlaceResponseDto;
import com.oath.domain.plan.repository.PlanJpaRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final MemberRepository memberRepository;

    private final ChatRepository chatRepository;

    private final SummaryService summaryService;

    private final MemberService memberService;

    private final GroupRepository groupRepository;

    private final GroupMemberRepository groupMemberRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final PlanJpaRepository planJpaRepository;


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
        return "redirect:/api/admin/member-list?page=" + page;
    }

//    @GetMapping("/members")
//    public Page<AdminResponse.MemberDto> getMembers(Pageable pagable) {
//        Page<AdminResponse.MemberDto> members = adminService.getMembers(pagable);
//        return members;
//    }

    @PostMapping("/update-role")
    public String updateRole(@RequestParam Long id, @RequestParam String role, @RequestParam int page) {
            adminService.updateRole(id, role);
            return "redirect:/api/admin/member-list?page=" + page;
    }

    @GetMapping("/member-list")
    public String getMembers(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String keyword,
                             Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.MemberDto> memberPage = adminService.getMembers(type, keyword, pageable);
                //memberRepository.findAll(pageable).map(member -> new AdminResponse.MemberDto(member));

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);

        List<AdminResponse.PageDto> pages = IntStream.range(0, memberPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);

        return "memberList";
    }



    @GetMapping("/group-list")
    public String getGroupList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "group") String type,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.GroupList> groupPage;

        if(keyword != null && !keyword.isEmpty()){
            if ("member".equals(type)) {
                groupPage = adminService.searchGroupListByMemberEmail(keyword, pageable);
            } else { // type = group (기본)
                groupPage = adminService.searchGroupList(keyword, pageable);
            }
        }else {
            groupPage = adminService.getGroupList(pageable);
        }

        Long groupCount = groupRepository.getTotalGroupCount();

        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("groupCount", groupCount);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("isMember", "member".equals(type));
        model.addAttribute("isGroup", "group".equals(type));


        List<AdminResponse.PageDto> pages = IntStream.range(0, groupPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);
        return "groupList";
    }

    @GetMapping("/chat-list/{groupId}")
    public String getChat(@PathVariable Long groupId, Model model) throws IOException {
        List<AdminResponse.ChatMemberDto> chatMembers = adminService.chatMember(groupId);
        List<AdminResponse.ChatDto> chats = adminService.chatList(groupId);
        String summary = summarizeChat(groupId);
        model.addAttribute("chatMembers", chatMembers);
        model.addAttribute("summary", summary);
        model.addAttribute("chats", chats);
        return "chatList";
    }

    @GetMapping("/chat-list/{groupId}/detail")
    public String getChatDetail(@PathVariable Long groupId, Model model) {

        List<AdminResponse.PlanDto> plans = adminService.getPlanList(groupId);
        model.addAttribute("plans", plans);

        return "chat";
    }

    @GetMapping("/chat-list/{groupId}/{memberId}")
    @ResponseBody
    public List<AdminResponse.ChatDto> getChatByMember( @PathVariable Long groupId, @PathVariable Long memberId, Model model) {
        List<AdminResponse.ChatDto> chats = adminService.chatListByMember(groupId, memberId);

        return chats;
    }

    public String summarizeChat(@PathVariable Long roomId) throws IOException {
        // DB에서 채팅 불러오기
        List<Chat> chatEntities = chatRepository.findByGroupIdWithMember(roomId);
        System.out.println(chatEntities);

        // ChatMessage 로 변환
        List<String> chats = chatEntities.stream()
                .map(e -> e.getContent())
                .filter(c -> c != null)
                .collect(Collectors.toList());

        String summary = summaryService.summarizeChats(chats);
        return summary;
    }

    @GetMapping("/popular-plan-tag")
    @ResponseBody
    public List<AdminResponse.popularPlanTag> getPopularPlanTag(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);

        List<AdminResponse.popularPlanTag> popularPlanTags = adminService.getPopularPlanTag(startDateTime, endDateTime);

        return popularPlanTags;
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
    public List<AdminResponse.PlanTagPie> PlanTagPie() {
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminService.PlanTagPie();
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

    //막대그래프
    @GetMapping("/monthly-count")
    @ResponseBody
    public List<AdminResponse.MonthlyCount> getMonthlyCount() {
        return adminService.getMonthlyCount();
    }

    @GetMapping("/active-count")
    @ResponseBody
    public List<AdminResponse.activeCount> getActiveCount() {
        List<AdminResponse.activeCount> activeCount = adminService.getActiveCount();
        return activeCount;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }


    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> adminLogin(@RequestBody MemberLoginDto dto) {
        Member member = memberService.login(dto); // 공통 로그인 사용
        checkAdmin(member);

        String token = jwtTokenProvider.createToken(
                member.getEmail(),
                member.getRole(),
                member.getId()
        );

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(CommonResponse.success(
                        Map.of(
                            "memberId", member.getId(),
                            "email", member.getEmail(),
                            "role", member.getRole()
                                ),
                        "로그인 성공"
                ));
    }

    private void checkAdmin(Member member) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new Exception401("관리자 권한이 없습니다.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https 환경만
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 즉시 삭제

        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(
            @CookieValue("accessToken") String token,
            @RequestBody Map<String, String> req) {

        if(token == null || token.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "로그인 필요"));
        }

        Long adminId = jwtTokenProvider.getMemberId(token); // JWT에서 ID 추출
        String password = req.get("password");

        boolean valid = memberService.checkPassword(adminId, password);

        return ResponseEntity.ok(Map.of("success", valid));
    }

    @GetMapping("/dashboard")
    public String getDashBoard(Model model) {
        Long member= memberRepository.count();
        Long group= groupRepository.count();
        Long chat= chatRepository.count();
        Long plan = planJpaRepository.count();

        model.addAttribute("member", member);
        model.addAttribute("group", group);
        model.addAttribute("chat", chat);
        model.addAttribute("plan", plan);

        return "dashboard";
    }

    @GetMapping("/tag-page")
    public String tagPage() {
        return "tagpage";
    }

    @GetMapping("/tag")
    @ResponseBody
    public List<String> getTagList() {
        List<String> tags = adminService.getTagList();
        return tags;
    }

    @PostMapping("/tag")
    public void addTag(@RequestBody AdminRequest.TagRequest req) {
        adminService.addTag(req.getName());
    }

    @DeleteMapping("/tag/{name}")
    public void deleteTag(@PathVariable String name) {
        adminService.deleteTag(name);
    }

    @GetMapping("/place-page")
    public String getPlaceBoard(Model model) {
        return "placepage";
    }

    @GetMapping("/place")
    @ResponseBody
    public List<AdminResponse.placeList> getPlaceList() {
        List<AdminResponse.placeList> places = adminService.getPlaceList();
        return places;
    }

    @GetMapping("/place-tag")
    public String getPlaceTag(Model model) {
        return "placetag";
    }

    @PostMapping("/place/desc/{placeId}")
    public void updateDescription (@PathVariable Long placeId, @RequestBody AdminRequest.updateDescription req) {
        adminService.updateDescription(placeId, req);
    }

    @GetMapping("/place-tag-list")
    @ResponseBody
    public List<AdminResponse.PlaceTag> getPlaceTag() {
        List<AdminResponse.PlaceTag> placeTags = adminService.getPlaceTag();
        return placeTags;
    }

    @PostMapping("/add/place-tags")
    @ResponseBody
    public List<AdminResponse.AddedTagDto> addPlaceTag(@RequestBody AdminRequest.PlaceTag req) {
        List<AdminResponse.AddedTagDto> tagDto = adminService.addPlaceTag(req);
        return tagDto;
    }

    @DeleteMapping("/place-tag/{placeId}/{tagId}")
    @ResponseBody
    public void deletePlaceTag(@PathVariable Long placeId, @PathVariable Long tagId) {
        adminService.deletePlaceTag(placeId, tagId);
    }


    @GetMapping("/search")
    public String searchPlace(@RequestParam String keyword) {
        PlaceResponseDto.PlaceDto place = adminService.searchPlace(keyword);
        return "place";
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePlace(@RequestBody AdminRequest.PlaceDto requestDto) {
        Place place = adminService.savePlace(requestDto);
        return ResponseEntity.ok(place);
    }

    @GetMapping("/plan-count")
    @ResponseBody
    public List<AdminResponse.PlanCount> getPlanCount() {
        List<AdminResponse.PlanCount> planCount = adminService.getPlanCount();
        return planCount;
    }

    @GetMapping("/par-count")
    @ResponseBody
    public List<AdminResponse.ParticipantCount> getParticipantCount() {
        List<AdminResponse.ParticipantCount> participantCount = adminService.getParticipantCount();
        return participantCount;
    }
}
