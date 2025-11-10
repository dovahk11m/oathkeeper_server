package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(2)
public class DataInitializer2_Member implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 사용자 데이터를 생성 시작");

        createMember("user1@test.com", "김철수", "1234", Role.USER);
        createMember("user2@test.com", "이영희", "1234", Role.USER);
        createMember("user3@test.com", "박민철", "1234", Role.USER);
        createMember("user4@test.com", "최상혁", "1234", Role.USER);
        createMember("user5@test.com", "정민지", "1234", Role.USER);
        createMember("admin@test.com", "관리자", "1234", Role.ADMIN);

        log.info("👷‍♂️ 샘플 사용자 데이터 생성 완료");
    }

    private Member createMember(String email, String username, String password, Role role) {
        // 회원마다 고유한 seed 생성 (email 기반)
        String seed = UUID.nameUUIDFromBytes(email.getBytes()).toString();

        // seed 기반 고정 이미지 URL
        String profileImageUrl = "https://picsum.photos/seed/" + seed + "/200/200";

        return memberRepository.save(Member.builder()
                .email(email)
                .username(username)
                .profileImageUrl(profileImageUrl)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(com.oath.domain.members.domain.Status.ACTIVE)
                .socialType(SocialType.LOCAL)
                .socialId(email)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build());
    }
}
