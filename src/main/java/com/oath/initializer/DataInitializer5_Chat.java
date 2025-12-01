package com.oath.initializer;

import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(5)
public class DataInitializer5_Chat {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 채팅 데이터 생성 시작");

        // 1. 필요한 엔티티 조회
        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow(); // 김철수
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow(); // 이영희
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow(); // 박민철
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow(); // 최상혁
        Group sampleGroup = groupRepository.findByName("샘플 그룹")
                .orElseGet(() -> groupRepository.save(Group.builder()
                        .name("샘플 그룹")
                        .createdAt(LocalDateTime.now())
                        .build()
                ));


        // 2. 채팅 데이터 생성
        List<Chat> chats = List.of(
                createChat(sampleGroup, user1, "안녕하세요! 이번 주말에 다들 시간 괜찮으신가요?", 8, 10),
                createChat(sampleGroup, user2, "네, 저는 좋아요! 뭐 할까요?", 8, 8),
                createChat(sampleGroup, user3, "오랜만에 다 같이 치맥 어떠세요?", 8, 7),
                createChat(sampleGroup, user4, "치맥 너무 좋죠!! 어디서 모일까요?", 8, 6),
                createChat(sampleGroup, user1, "다들 중간 지점인 서면이 편하지 않을까요?", 8, 5),
                createChat(sampleGroup, user2, "서면 좋네요. 그럼 서면에서 맛있는 치킨집 찾아볼까요?", 8, 4),
                createChat(sampleGroup, user3, "알겠습니다! 제가 찾아보고 공유 드릴게요!", 8, 2),
                createChat(sampleGroup, user4, "네! 기대하고 있겠습니다!", 8, 1)
        );

        chatRepository.saveAll(chats);

        log.info("👷‍♂️ 샘플 채팅 데이터 {}건 생성 완료", chats.size());
    }

    private Chat createChat(Group group, Member sender, String content, int daysAgo, int minutesAgo) {
        return Chat.builder()
                .group(group)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now().minusDays(daysAgo).minusMinutes(minutesAgo))
                .build();
    }
}
