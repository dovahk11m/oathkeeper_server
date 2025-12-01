package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
public class DataInitializer2_Member {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void initialize() throws Exception {

        log.info("👷‍♂️ 샘플 사용자 데이터를 생성 시작");

        createMember("user1@test.com", "김철수", "1234", Role.USER, "부산광역시 금정구 부산대학로63번길 2", 35.2335, 129.0814, "/profile/user1.png");
        createMember("user2@test.com", "이영희", "1234", Role.USER, "부산광역시 부산진구 부전동", 35.1577, 129.0591, "/profile/user2.png");
        createMember("user3@test.com", "박민철", "1234", Role.USER, "부산광역시 해운대구 우동", 35.1631, 129.1636, "/profile/user3.png");
        createMember("user4@test.com", "최상혁", "1234", Role.USER, "부산광역시 수영구 광안동", 35.1531, 129.1187, "/profile/user4.png");
        createMember("user5@test.com", "정민지", "1234", Role.USER, "부산광역시 동래구 온천동", 35.2031, 129.0802, "/profile/user5.png");
        createMember("admin@test.com", "관리자", "1234", Role.ADMIN, "부산광역시청", 35.1796, 129.0756, "/profile/admin.png");
        createMember("choongechobiz@gmail.com", "조충희", "1234", Role.ADMIN, "부산광역시 연제구 연산동", 35.18, 129.07, ""); // 부산시청과 동일한 위도/경도 사용

        log.info("👷‍♂️ 샘플 사용자 데이터 생성 완료");
    }


    private Member createMember(String email, String username, String password, Role role, String address, double lat, double lng, String profileImageUrl) {
        return memberRepository.save(Member.builder()
                .email(email)
                .username(username)
                .profileImageUrl(profileImageUrl)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(com.oath.domain.members.domain.Status.ACTIVE)
                .socialType(SocialType.LOCAL)
                .socialId(email)
                .defaultAddress(address)
                .defaultLat(lat)
                .defaultLng(lng)
                .createdAt(LocalDateTime.now().minusDays(10)) // 10일 전 가입
                .updatedAt(LocalDateTime.now().minusDays(5))  // 5일 전 수정
                .lastLogin(LocalDateTime.now().minusDays(1))  // 1일 전 마지막 로그인
                 .profileImageUrl(profileImageUrl)
                 .build());
    }
}
