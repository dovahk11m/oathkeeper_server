package com.oath.domain.members.controller;

import com.oath.common.JwtTokenProvider;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception401;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.service.MemberService;
import com.oath.domain.members.service.SummaryService;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceResponseDto;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.visitors.VisitorService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpHeaders;
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

    private final JwtTokenProvider jwtTokenProvider;

    private final PlanJpaRepository planJpaRepository;

    private final VisitorService visitorService;

    @Auth
    @PostMapping("/ban-member")
    public String banMember(@RequestParam Long id, @RequestParam int days, @RequestParam int page){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        adminService.banMember(member, days);
        return "redirect:/api/admin/member-list?page=" + page;
    }

    @Auth
    @PostMapping("/update-role")
    public String updateRole(@RequestParam Long id, @RequestParam String role, @RequestParam int page) {
            adminService.updateRole(id, role);
            return "redirect:/api/admin/member-list?page=" + page;
    }

    @Auth
    @GetMapping("/member-list")
    public String getMembers(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String keyword,
                             Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.MemberDto> memberPage = adminService.getMembers(type, keyword, pageable);

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);

        List<AdminResponse.PageDto> pages = IntStream.range(0, memberPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);

        return "memberList";
    }

    @Auth
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

    @Auth
    @GetMapping("/chat-list/{groupId}")
    public String getChat(@PathVariable Long groupId, Model model) throws IOException {
        List<AdminResponse.ChatMemberDto> chatMembers = adminService.chatMember(groupId);
        List<AdminResponse.ChatDto> chats = adminService.getChat(groupId);
        List<AdminResponse.ChatListDto> chatLists = adminService.chatList(groupId);

        String summary = summarizeChat(groupId);
        model.addAttribute("groupId", groupId);
        model.addAttribute("chatLists", chatLists);
        model.addAttribute("chatMembers", chatMembers);
        model.addAttribute("summary", summary);
        model.addAttribute("chats", chats);
        return "chatList";
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


    @Auth
    @GetMapping("/plan-tag-pie")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlanTagPie>>> PlanTagPie() {
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminService.PlanTagPie();
        return ResponseEntity.ok(CommonResponse.success(PlanTagsPie));
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<CommonResponse<Object>> adminLogin(@RequestBody MemberLoginDto dto) {
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

    @Auth
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https 환경만
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 즉시 삭제

        response.addCookie(cookie);
        return "redirect:/api/admin/login";
    }

    @Auth
    @PostMapping("/check-password")
    public ResponseEntity<CommonResponse<Object>> checkPassword(
            @CookieValue("accessToken") String token,
            @RequestBody Map<String, String> req) {

        if(token == null || token.isEmpty()) {
            return new ResponseEntity<>(CommonResponse.error("로그인 필요"), HttpStatus.UNAUTHORIZED);
        }

        Long adminId = jwtTokenProvider.getMemberId(token); // JWT에서 ID 추출
        String password = req.get("password");
        Long groupId = Long.valueOf(req.get("groupId"));

        boolean valid = memberService.checkPassword(adminId, password);

        visitorService.saveVisitor(groupId, adminId);

        return ResponseEntity.ok(CommonResponse.success(valid));
    }

    @Auth
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

    @Auth
    @GetMapping("/tag-page")
    public String tagPage() {
        return "tagpage";
    }

    @Auth
    @GetMapping("/tag")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<String>>> getTagList() {
        List<String> tags = adminService.getTagList();
        return ResponseEntity.ok(CommonResponse.success(tags));
    }

    @Auth
    @PostMapping("/tag")
    public ResponseEntity<CommonResponse<Object>> addTag(@RequestBody AdminRequest.TagRequest req) {
        adminService.addTag(req.getName());
        return ResponseEntity.ok(CommonResponse.success(null, "태그가 추가되었습니다."));
    }

    @Auth
    @DeleteMapping("/tag/{name}")
    public ResponseEntity<CommonResponse<Object>> deleteTag(@PathVariable String name) {
        adminService.deleteTag(name);
        return ResponseEntity.ok(CommonResponse.success(null, "태그가 삭제되었습니다."));
    }

    @Auth
    @GetMapping("/place-page")
    public String getPlaceBoard(Model model) {
        return "placepage";
    }

    @Auth
    @GetMapping("/place")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.placeList>>> getPlaceList() {
        List<AdminResponse.placeList> places = adminService.getPlaceList();
        return ResponseEntity.ok(CommonResponse.success(places));
    }

    @Auth
    @DeleteMapping("/place/{placeId}")
    public ResponseEntity<CommonResponse<Object>> deletePlace(@PathVariable Long placeId) {
        adminService.deletePlace(placeId);
        return ResponseEntity.ok(CommonResponse.success(null, "장소가 삭제되었습니다."));
    }

    @Auth
    @GetMapping("/place-tag")
    public String getPlaceTag(Model model) {
        return "placetag";
    }

    @Auth
    @PostMapping("/place/desc/{placeId}")
    public ResponseEntity<CommonResponse<Object>> updateDescription (@PathVariable Long placeId, @RequestBody AdminRequest.updateDescription req) {
        adminService.updateDescription(placeId, req);
        return ResponseEntity.ok(CommonResponse.success(null, "장소 상세가 수정되었습니다."));
    }

    @Auth
    @GetMapping("/place-tag-list")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlaceTag>>> getPlaceTag() {
        List<AdminResponse.PlaceTag> placeTags = adminService.getPlaceTag();
        return ResponseEntity.ok(CommonResponse.success(placeTags));
    }

    @Auth
    @PostMapping("/add/place-tags")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.AddedTagDto>>> addPlaceTag(@RequestBody AdminRequest.PlaceTag req) {
        List<AdminResponse.AddedTagDto> tagDto = adminService.addPlaceTag(req);
        return ResponseEntity.ok(CommonResponse.success(tagDto));
    }

    @Auth
    @DeleteMapping("/place-tag/{placeId}/{tagId}")
    @ResponseBody
    public ResponseEntity<CommonResponse<Object>> deletePlaceTag(@PathVariable Long placeId, @PathVariable Long tagId) {
        adminService.deletePlaceTag(placeId, tagId);
        return ResponseEntity.ok(CommonResponse.success(null, "장소-태그가 삭제되었습니다."));
    }

    @Auth
    @GetMapping("/search")
    public String searchPlace(@RequestParam String keyword) {
        PlaceResponseDto.PlaceDto place = adminService.searchPlace(keyword);
        return "place";
    }

    @Auth
    @PostMapping("/save")
    public ResponseEntity<CommonResponse<Place>> savePlace(@RequestBody AdminRequest.PlaceDto requestDto) {
        Place place = adminService.savePlace(requestDto);
        return ResponseEntity.ok(CommonResponse.success(place));
    }

    @Auth
    @GetMapping("/plan-count")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlanCount>>> getPlanCount() {
        List<AdminResponse.PlanCount> planCount = adminService.getPlanCount();
        return ResponseEntity.ok(CommonResponse.success(planCount));
    }

    @Auth
    @GetMapping("/par-count")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.ParticipantCount>>> getParticipantCount() {
        List<AdminResponse.ParticipantCount> participantCount = adminService.getParticipantCount();
        return ResponseEntity.ok(CommonResponse.success(participantCount));
    }
}
