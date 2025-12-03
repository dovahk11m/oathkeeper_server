package com.oath.domain.members.controller;

import com.oath.common.JwtTokenProvider;
import com.oath.common.auth.Auth;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.members.service.AdminService;
import com.oath.domain.members.service.MemberService;
import com.oath.domain.members.service.SummaryService;
import com.oath.domain.place_tag_plan.place.PlaceResponseDto;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.visitors.VisitorService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminService adminService;

    private final MemberRepository memberRepository;

    private final ChatRepository chatRepository;

    private final SummaryService summaryService;

    private final GroupRepository groupRepository;

    private final PlanJpaRepository planJpaRepository;

    @Auth
    @PostMapping("/members/ban")
    public String banMember(@RequestParam Long id, @RequestParam int days, @RequestParam int page){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        adminService.banMember(member, days);
        return "redirect:/admin/members?page=" + page;
    }

    @Auth
    @PostMapping("/members/role")
    public String updateRole(@RequestParam Long id, @RequestParam String role, @RequestParam int page) {
        adminService.updateRole(id, role);
        return "redirect:/admin/members?page=" + page;
    }

    @Auth
    @GetMapping("/members")
    public String getMembers(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String keyword,
                             Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AdminResponse.MemberDto> memberPage = adminService.getMembers(type, keyword, pageable);
        long count = memberPage.getTotalElements();

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("memberCount", count);

        List<AdminResponse.PageDto> pages = IntStream.range(0, memberPage.getTotalPages())
                .mapToObj(i -> new AdminResponse.PageDto(i + 1, i, i == page))
                .collect(Collectors.toList());

        model.addAttribute("pages", pages);

        return "memberList";
    }

    @Auth
    @GetMapping("/groups")
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
    @GetMapping("/groups/{groupId}/chats")
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

    @GetMapping("/login")
    public String login() {
        return "login";
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
        return "redirect:/admin/login";
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
    @GetMapping("/tags")
    public String tagPage() {
        return "tagpage";
    }

    @Auth
    @GetMapping("/places")
    public String getPlaceBoard(Model model) {
        return "placepage";
    }

    @Auth
    @GetMapping("/places/tags")
    public String getPlaceTag(Model model) {
        return "placetag";
    }

    @Auth
    @GetMapping("/search")
    public String searchPlace(@RequestParam String keyword) {
        PlaceResponseDto.PlaceDto place = adminService.searchPlace(keyword);
        return "place";
    }

}
