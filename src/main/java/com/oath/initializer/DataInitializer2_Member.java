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

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(2)
public class DataInitializer2_Member implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 사용자 데이터를 생성 시작");

        createMember("user1@test.com", "김철수", "1234", Role.USER, "부산광역시 금정구 부산대학로63번길 2", 35.2335, 129.0814);
        createMember("user2@test.com", "이영희", "1234", Role.USER, "부산광역시 부산진구 부전동", 35.1577, 129.0591);
        createMember("user3@test.com", "박민철", "1234", Role.USER, "부산광역시 해운대구 우동", 35.1631, 129.1636);
        createMember("user4@test.com", "최상혁", "1234", Role.USER, "부산광역시 수영구 광안동", 35.1531, 129.1187);
        createMember("user5@test.com", "정민지", "1234", Role.USER, "부산광역시 동래구 온천동", 35.2031, 129.0802);
        createMember("admin@test.com", "관리자", "1234", Role.ADMIN, "부산광역시청", 35.1796, 129.0756);

        log.info("👷‍♂️ 샘플 사용자 데이터 생성 완료");
    }

    private Member createMember(String email, String username, String password, Role role, String address, double lat, double lng) {
        return memberRepository.save(Member.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(com.oath.domain.members.domain.Status.ACTIVE)
                .socialType(SocialType.LOCAL)
                .socialId(email)
                .defaultAddress(address)
                .defaultLat(lat)
                .defaultLng(lng)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build());
    }
}
