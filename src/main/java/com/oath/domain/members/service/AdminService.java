package com.oath.domain.members.service;

import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.ActiveChartDto;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.repository.AdminRepository;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    private final MemberRepository memberRepository;

    private final GroupRepository groupRepository;

    private final ChatRepository chatRepository;

    public void banMember(Member member, int days) {
        LocalDateTime now = LocalDateTime.now();

        if (member.getStatus() == Status.SUSPENDED && member.getBannedUntil() != null && member.getBannedUntil().isAfter(now)) {
            member.setBannedUntil(member.getBannedUntil().plusDays(days));
        } else {
            member.setStatus(Status.SUSPENDED);
            member.setBannedUntil(now.plusDays(days));
        }
        memberRepository.save(member);
    }

//    public AdminResponse.ListDto list() {
//        List<AdminResponse.MemberDto> memberDtos = memberRepository.findAll().stream()
//                .map(m -> new AdminResponse.MemberDto(m))
//                .collect(Collectors.toList());
//
//        AdminResponse.ListDto response = new AdminResponse.ListDto();
//        response.setMembers(memberDtos);
//
//        return response;
//    }

    public Page<AdminResponse.MemberDto> getMembers(Pageable pageable) {
        Page<AdminResponse.MemberDto> members = memberRepository.findAll(pageable)
                .map(m -> new AdminResponse.MemberDto(m));


        return members;
    }

    public void updateRole(Long id, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

                member.setRole(Role.valueOf(role));
        memberRepository.save(member);
    }

    public List<AdminResponse.popularPlanTag> getPopularPlanTag(LocalDateTime startDate, LocalDateTime endDate) {
        PageRequest topTen = PageRequest.of(0, 10);
        List<AdminResponse.popularPlanTag> popularPlanTags = adminRepository.populrPlanTag(startDate, endDate, topTen);
        return popularPlanTags;
    }

    public List<AdminResponse.PlanTagPie> PlanTagPie(LocalDateTime startDate, LocalDateTime endDate) {
        PageRequest topTen = PageRequest.of(0, 10);
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminRepository.PlanTagPie(startDate, endDate, topTen);
        return PlanTagsPie;
    }

    public Page<AdminResponse.groupListDto> getGroupList(Pageable pageable) {
        Page<AdminResponse.groupListDto> groups = groupRepository.findAll(pageable)
                .map(g -> new AdminResponse.groupListDto(g));
        return groups;
    }

    public List<ActiveChartDto> activeCharts() {
        List<ActiveChartDto> activeChart = adminRepository.activeChart();
        return activeChart;
    }

    public List<AdminResponse.ChatDto> chatList(Long groupId){
        List<AdminResponse.ChatDto> chats = chatRepository.findByGroupIdOrderBySentAt(groupId)
                .stream()
                .map(m -> new AdminResponse.ChatDto(m))
                .collect(Collectors.toList());
        return chats;
    }

    public List<AdminResponse.ChatMemberDto> chatMember(Long groupId) {
        List<AdminResponse.ChatMemberDto> chatMembers = adminRepository.chatMember(groupId);
        return chatMembers;
    }

    public List<AdminResponse.ChatDto> chatListByMember (Long groupId, Long memberId){
        List<AdminResponse.ChatDto> chats = chatRepository.findByGroup_IdAndSender_IdOrderBySentAt(groupId, memberId)
                .stream()
                .map(m -> new AdminResponse.ChatDto(m))
                .collect(Collectors.toList());
        return chats;
    }



}
