package com.oath.domain.chat.view;

import com.oath.domain.chat.Chat;
import com.oath.domain.chat.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)  // 읽기 전용 트랜잭션 상태
@RequiredArgsConstructor
@Service
public class ChatViewService {

    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    // 채팅 메세지 저장
    @Transactional
    public Chat save(String msg) {
        Group sampleGroup = groupRepository.findById(1L).orElseThrow(() -> new IllegalStateException("테스트용 그룹(ID:1)을 찾을 수 없습니다."));
        Member sampleSender = memberRepository.findById(1L).orElseThrow(() -> new IllegalStateException("테스트용 사용자(ID:1)를 찾을 수 없습니다."));

        // [수정] sentAt이 자동으로 설정되는 정적 팩토리 메서드를 사용합니다.
        Chat savedChat = chatRepository.save(Chat.of(sampleGroup, sampleSender, msg, null));
        return savedChat;
    }

    // 채팅 메세지 리스트
    public List<Chat> findAll() {
        // 내림 차순으로 정렬하고 싶다면
        Sort asc = Sort.by(
                Sort.Direction.ASC,
                "id"
        );
        return chatRepository.findAllWithGroupAndSender(asc);
    }

}
