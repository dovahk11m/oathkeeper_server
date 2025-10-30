package com.oath.domain.members.service;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.repository.AdminRepository;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    private final MemberRepository memberRepository;

    public void banMember(Member member, int days) {
        member.setStatus(Status.SUSPENDED);
        member.setBannedUntil(LocalDateTime.now().plusDays(days));
        memberRepository.save(member);
    }

    public AdminResponse.ListDto list() {
        List<AdminResponse.MemberDto> memberDtos = memberRepository.findAll().stream()
                .map(m -> new AdminResponse.MemberDto(m))
                .collect(Collectors.toList());

        AdminResponse.ListDto response = new AdminResponse.ListDto();
        response.setMembers(memberDtos);

        return response;
    }

    public Page<AdminResponse.MemberDto> getMembers(Pageable pageable) {
        Page<AdminResponse.MemberDto> members = memberRepository.findAll(pageable)
                .map(m -> new AdminResponse.MemberDto(m));
        return members;
    }
}
