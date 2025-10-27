package com.oath.domain.members.service;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.MemberCreateDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.dto.MemberRequest;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    public Member create(MemberCreateDto memberCreateDto){
        Member member = Member.builder()
                .username(memberCreateDto.getUsername())
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
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

        if(member.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if(member.getLastLogin().isBefore(LocalDateTime.now().minusYears(1))) {
            member.setStatus(Status.INACTIVE);
            memberRepository.save(member);
            throw new IllegalArgumentException("휴면계정입니다.");
        }

        member.setLastLogin(LocalDateTime.now());
        memberRepository.save(member);
        return member;
    }

    public Member getMemberBySocialId(String socialId){
        Member member = memberRepository.findBySocialId(socialId).orElse(null);
        return member;
    }

    public Member createOauth(String socialId, String email, SocialType socialType, String nickname) {
        Member member = Member.builder()
                .username(nickname)
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .createdAt(LocalDateTime.now())
                .build();
        memberRepository.save(member);
        return member;
    }

    @Transactional(readOnly = true)
    public MemberResponse.DTO getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));
        return new MemberResponse.DTO(member);
    }


    public MemberResponse.DTO updateMember(Long memberId, MemberRequest.Update request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));
        member.updateInfo(request.getEmail());
        return new MemberResponse.DTO(member);
    }


    public void updatePassword(Long memberId, MemberRequest.PasswordUpdate request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));

    }


    public String findId(MemberRequest.FindId request) {
         Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));
        return member.getUsername();
    }

    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));
        member.deactivate();
    }


    private String generateTempPassword() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 8);
    }

    public void sendTemporaryPassword(MemberRequest.FindPassword request) {
        memberRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .ifPresentOrElse(
                        m -> {
                            String tempPassword = generateTempPassword();
                            m.updatePassword(passwordEncoder.encode(tempPassword));

                            // 이메일 발송
                            emailService.sendMail(
                                    m.getEmail(),
                                    "[서비스명] 임시 비밀번호 안내",
                                    "안녕하세요 " + m.getUsername() + "님.\n\n" +
                                            "요청하신 임시 비밀번호는 다음과 같습니다:\n\n" +
                                            tempPassword + "\n\n" +
                                            "로그인 후 반드시 비밀번호를 변경해주세요."
                            );
                        },
                        () -> { throw new IllegalArgumentException("일치하는 회원이 없습니다."); }
                );
    }


}
