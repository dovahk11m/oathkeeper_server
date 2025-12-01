package com.oath.domain.members.service;

import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception404;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.oath.common.JwtTokenProvider;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception409;
import com.oath.common.exception.Exception500;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.MemberCreateDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.dto.MemberRequest;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.memberEvent.MemberSignupEvent;
import com.oath.domain.members.memberEvent.PasswordResetEvent;
import com.oath.domain.members.memberEvent.SocialSignupEvent;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.terms.TermService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional("h2TransactionManager")
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CacheManager cacheManager;
    private final TermService termService;
    private final ApplicationEventPublisher eventPublisher;

    private final String UPLOAD_DIR = "./uploads/profile/";
    private final String WEB_PATH_PREFIX = "/profile-images/";

    public Member create(MemberCreateDto memberCreateDto) {
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()) {
            throw new Exception409("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .username(memberCreateDto.getUsername())
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .role(Role.USER)
                .status(Status.INACTIVE)
                .socialType(SocialType.LOCAL)
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        member.generateEmailVerificationToken();
        Member savedMember = memberRepository.save(member);

        termService.agreeTerms(memberCreateDto.getAgreedTermIds(), savedMember);

        // 이메일 가입 이벤트 발행
        eventPublisher.publishEvent(new MemberSignupEvent(savedMember.getEmail(), savedMember.getUsername(),
                savedMember.getEmailVerificationToken()));

        return savedMember;
    }


    public void verifyEmail(String token) {
        Member member = memberRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new Exception404("유효하지 않은 인증 토큰입니다."));

        if (member.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new Exception400("인증 토큰이 만료되었습니다. 다시 가입해주세요.");
        }

        member.activate();
    }

    @Transactional(readOnly = true)
    public Member login(MemberLoginDto memberLoginDto) {
        Member member = memberRepository.findByEmail(memberLoginDto.getEmail())
                .orElseThrow(() -> new Exception401("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new Exception401("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        postLogin(member);

        return member;
    }

    // 로그아웃
    public void logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new Exception400("유효하지 않은 토큰입니다.");
        }

        long remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);

        var cache = cacheManager.getCache("blacklistedTokens");
        if (cache != null) {
            cache.put(token, remainingExpiration);
        }
    }


    public void postLogin(Member member) {
        if (member.getStatus() == Status.INACTIVE) {
            throw new Exception401("이메일 인증이 완료되지 않은 계정입니다. 이메일을 확인해주세요.");
        }
        if (member.getStatus() != Status.ACTIVE) {
            throw new Exception401("사용이 중지된 계정입니다.");
        }

        if (member.getLastLogin().isBefore(LocalDateTime.now().minusYears(1))) {
            member.setStatus(Status.INACTIVE);
            memberRepository.save(member);

            throw new Exception401("휴면계정입니다. 다시 로그인하여 활성화해주세요.");

        }

        member.setLastLogin(LocalDateTime.now());
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberBySocialId(String socialId) {
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
        Member savedMember = memberRepository.save(member);

        // 소셜 가입 이벤트 발행
        eventPublisher.publishEvent(new SocialSignupEvent(savedMember));

        return savedMember;
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
            throw new Exception401("현재 비밀번호가 일치하지 않습니다.");
        }
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));

    }

    @Transactional(readOnly = true)
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

                            // 비밀번호 재설정 이벤트 발행
                            eventPublisher
                                    .publishEvent(new PasswordResetEvent(m.getEmail(), m.getUsername(), tempPassword));
                        },

                        () -> {
                            throw new Exception404("일치하는 회원이 없습니다.");
                        });
    }

    public String uploadProfileImage(MultipartFile image, Long memberId) throws IOException {

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));

        // 2. 기존 이미지 삭제
        try {
            String oldImageUrl = member.getProfileImageUrl();
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                String oldFileName = oldImageUrl.replace(WEB_PATH_PREFIX, "");
                Path oldPath = Paths.get(UPLOAD_DIR + oldFileName);
                Files.deleteIfExists(oldPath);
            }
        } catch (IOException e) {
            throw new Exception500("기존 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        Files.createDirectories(filePath.getParent());
        Files.copy(image.getInputStream(), filePath);

        String newImageUrl = WEB_PATH_PREFIX + fileName;
        member.setProfileImageUrl(newImageUrl);

        return newImageUrl;
    }

    public void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));

        member.setProfileImageUrl(null);

        memberRepository.save(member);
    }

    public void countJoin() {

    }

    @Transactional(readOnly = true)
    public boolean checkPassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("일치하는 회원이 없습니다."));
        return passwordEncoder.matches(password, member.getPassword());
    }

}
