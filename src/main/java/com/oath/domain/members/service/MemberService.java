package com.oath.domain.members.service;

import com.oath.common.JwtTokenProvider;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception404;
import com.oath.common.exception.Exception409;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.MemberCreateDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.dto.MemberRequest;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.memberEvent.MemberSignupEvent;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.terms.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CacheManager cacheManager;
    private final TermService termService;
    private final ApplicationEventPublisher eventPublisher;

    public Member create(MemberCreateDto memberCreateDto){
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()) {
            throw new Exception409("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .username(memberCreateDto.getUsername())
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .role(Role.USER)
                .status(Status.INACTIVE)
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        member.generateEmailVerificationToken();
        Member savedMember = memberRepository.save(member);

        termService.agreeTerms(memberCreateDto.getAgreedTermIds(), savedMember);

        // 이벤트 발행
        eventPublisher.publishEvent(new MemberSignupEvent(savedMember.getEmail(), savedMember.getUsername(), savedMember.getEmailVerificationToken()));

        return savedMember;
    }

    @Transactional
    public void verifyEmail(String token) {
        Member member = memberRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new Exception404("유효하지 않은 인증 토큰입니다."));

        if (member.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new Exception400("인증 토큰이 만료되었습니다. 다시 가입해주세요.");
        }

        member.activate();
    }

    public Member login(MemberLoginDto memberLoginDto){
        Member member = memberRepository.findByEmail(memberLoginDto.getEmail())
                .orElseThrow(() -> new Exception401("이메일 또는 비밀번호가 일치하지 않습니다."));

        if(!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new Exception401("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        postLogin(member);

        return member;
    }

    //로그아웃
    public void logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new Exception400("유효하지 않은 토큰입니다.");
        }

        long remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);

        cacheManager.getCache("blacklistedTokens").put(token, remainingExpiration);
    }

    public Member postLogin(Member member) {
        if(member.getStatus() == Status.INACTIVE) {
            throw new Exception401("이메일 인증이 완료되지 않은 계정입니다. 이메일을 확인해주세요.");
        }
        if(member.getStatus() != Status.ACTIVE) {
            throw new Exception401("사용이 중지된 계정입니다.");
        }

        if(member.getLastLogin().isBefore(LocalDateTime.now().minusYears(1))) {
            member.setStatus(Status.INACTIVE);
            memberRepository.save(member);
            throw new Exception401("휴면계정입니다. 다시 로그인하여 활성화해주세요.");
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
                .status(Status.ACTIVE)
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        memberRepository.save(member);
        return member;
    }

    @Transactional(readOnly = true)
    public MemberResponse.DTO getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        return new MemberResponse.DTO(member);
    }


    public MemberResponse.DTO updateMember(Long memberId, MemberRequest.Update request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        member.updateInfo(request.getUsername(), request.getProfileImageUrl(), request.getDefaultAddress());
        return new MemberResponse.DTO(member);
    }


    public void updatePassword(Long memberId, MemberRequest.PasswordUpdate request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));

    }

    public String findId(MemberRequest.FindId request) {
         Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        return member.getUsername();
    }

    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        member.deactivate();
    }

    private String generateTempPassword() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 8);
    }

    public void sendTemporaryPassword(MemberRequest.FindPassword request) {
        memberRepository.findByEmail(request.getEmail())
                .ifPresentOrElse(
                        m -> {
                            String tempPassword = generateTempPassword();
                            m.updatePassword(passwordEncoder.encode(tempPassword));

                            // 이메일 발송
                            // emailService.sendMail(...); // 이 부분도 이벤트 기반으로 변경 가능
                        },
                        () -> { throw new Exception404("일치하는 회원이 없습니다."); }
                );
    }

    public String uploadProfileImage (MultipartFile image, Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));

        String oldImageUrl = member.getProfileImageUrl();
        if (oldImageUrl != null) {
            Path oldPath = Paths.get("uploads/profile/" + oldImageUrl.replace("/profile-image", ""));
            Files.deleteIfExists(oldPath);
        }

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get("uploads/profile/" + fileName);

            Files.createDirectories(filePath.getParent());

            Files.copy(image.getInputStream(), filePath);

            String fileUrl = "/파일경로/" + fileName;

            member.setProfileImageUrl(fileUrl);

            memberRepository.save(member);

            return fileUrl;
    }

    public void deleteProfileImage (Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));

        member.setProfileImageUrl(null);

        memberRepository.save(member);
    }

    public void countJoin () {

    }

    public boolean checkPassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        return passwordEncoder.matches(password, member.getPassword());
    }

}
