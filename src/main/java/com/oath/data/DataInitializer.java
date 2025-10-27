package com.oath.data;

import com.oath.domain.chat.Chat;
import com.oath.domain.chat.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupService.GroupService;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 개발 환경에서 애플리케이션 시작 시 테스트용 샘플 데이터를 생성합니다.
 * 이 클래스는 'local' 프로필이 활성화된 경우에만 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("개발 환경 샘플 데이터를 생성합니다...");

        // 1. 샘플 사용자 생성
        Member user1 = createMember(
                "user1@test.com",
                "user1",
                "1234",
                Role.USER
        );
        Member user2 = createMember(
                "user2@test.com",
                "user2",
                "1234",
                Role.USER
        );
        Member admin = createMember(
                "admin@test.com",
                "admin",
                "1234",
                Role.ADMIN
        );

        // 2. 샘플 그룹 및 채팅방 생성 (GroupService 사용)
        Group sampleGroup = groupService.createGroup(
                new GroupCreateRequest("샘플 그룹"),
                user1.getEmail()
        );

        // 3. 그룹에 멤버 추가
        // GroupMember.of() 정적 팩토리 메서드를 사용하여 user2를 멤버로 추가합니다.
        groupMemberRepository.save(GroupMember.of(
                sampleGroup,
                user2
        ));

        // 5. 샘플 채팅 메시지 생성 (user1, user2가 모두 참여한 후)
        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user1,
                "안녕하세요! 샘플 데이터입니다.",
                null,
                LocalDateTime.now()
                        .minusMinutes(5)
        ));
        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user2,
                "네, 반갑습니다!",
                null,
                LocalDateTime.now()
                        .minusMinutes(4)
        ));

        log.info("샘플 데이터 생성이 완료되었습니다.");
    }

    private Member createMember(
            String email,
            String username,
            String password,
            Role role
    ) {
        return memberRepository.save(Member.builder()
                                             .email(email)
                                             .username(username)
                                             .password(passwordEncoder.encode(password))
                                             .role(role)
                                             .socialType(SocialType.LOCAL)
                                             .socialId(email)
                                             .createdAt(LocalDateTime.now())
                                             .updatedAt(LocalDateTime.now())
                                             .lastLogin(LocalDateTime.now())
                                             .build());
    }
}