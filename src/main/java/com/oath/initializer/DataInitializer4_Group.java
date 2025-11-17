package com.oath.initializer;

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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(4)
public class DataInitializer4_Group implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 그룹 데이터 생성 시작");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Member user5 = memberRepository.findByEmail("user5@test.com").orElseThrow();

        List<Member> memberList = List.of(user2, user3, user4, user5);

        Group sampleGroup = groupService.createGroup(
                new GroupCreateRequest("샘플 그룹"),
                user1.getEmail()
        );

        groupMemberRepository.saveAll(
                memberList.stream().map((member) -> GroupMember.of(sampleGroup, member)).toList()
        );

        log.info("👷‍♂️ 샘플 그룹 데이터 생성 완료");
    }
}
