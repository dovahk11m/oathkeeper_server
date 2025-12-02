package com.oath.initializer;

import java.util.List;
// import org.springframework.boot.CommandLineRunner; // 제거
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.dto.GroupCreateRequest;
import com.oath.domain.groups.repository.GroupMemberRepository;
import com.oath.domain.groups.service.GroupService;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
// @Transactional 제거 - 규칙 1: 트랜잭션 매니저 명시적 지정
@Profile("local")
// @Order 제거 - MasterDataInitializer에서 순서 통제
public class DataInitializer4_Group {

    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;


    // @Override 제거
    @Transactional("h2TransactionManager")
    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 그룹 데이터 생성 시작");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Member user5 = memberRepository.findByEmail("user5@test.com").orElseThrow();

        List<Member> memberList = List.of(user2, user3, user4, user5);

        Group sampleGroup1 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹1"), user1.getEmail());
        Group sampleGroup2 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹2"), user1.getEmail());
        Group sampleGroup3 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹3"), user1.getEmail());
        Group sampleGroup4 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹4"), user1.getEmail());
        Group sampleGroup5 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹5"), user1.getEmail());
        Group sampleGroup6 =
                groupService.createGroup(new GroupCreateRequest("샘플 그룹6"), user1.getEmail());

        groupMemberRepository.save(GroupMember.of(sampleGroup1, user2));
        groupMemberRepository.save(GroupMember.of(sampleGroup2, user2));
        groupMemberRepository.flush();

        groupMemberRepository.saveAll(
                memberList.stream().map((member) -> GroupMember.of(sampleGroup1, member)).toList());

        log.info("👷‍♂️ 샘플 그룹 데이터 생성 완료");

    }
}
