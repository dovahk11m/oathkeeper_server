package com.oath.domain.members.service;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.MemberCreateDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    public Member create(MemberCreateDto memberCreateDto){
        Member member = Member.builder()
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        return member;
    }

    public Member login(MemberLoginDto memberLoginDto){
        Optional<Member> optMember = memberRepository.findByEmail(memberLoginDto.getEmail());
        if(!optMember.isPresent()){
            throw new IllegalArgumentException("email이 존재하지 않습니다.");
        }

        Member member = optMember.get();
        if(!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("password가 일치하지 않습니다.");
        }
        return member;
    }

    public Member getMemberBySocialId(String socialId){
        Member member = memberRepository.findBySocialId(socialId).orElse(null);
        return member;
    }

    public Member createOauth(String socialId, String email, SocialType socialType) {
        Member member = Member.builder()
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .build();
        memberRepository.save(member);
        return member;
    }

}
