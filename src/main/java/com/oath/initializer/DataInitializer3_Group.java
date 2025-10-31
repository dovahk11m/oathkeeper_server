package com.oath.initializer;

import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupService.GroupService;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(2)
public class DataInitializer3_Group implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatRepository chatRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("샘플 그룹 및 채팅 데이터를 생성합니다...");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();

        Group sampleGroup = groupService.createGroup(
                new GroupCreateRequest("샘플 그룹"),
                user1.getEmail()
        );

        groupMemberRepository.save(GroupMember.of(
                sampleGroup,
                user2
        ));

        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user1,
                "안녕하세요! 샘플 데이터입니다.",
                null,
                LocalDateTime.now().minusMinutes(5)
        ));
        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user2,
                "네, 반갑습니다!",
                null,
                LocalDateTime.now().minusMinutes(4)
        ));

        log.info("샘플 그룹 및 채팅 데이터 생성이 완료되었습니다.");
    }
}
