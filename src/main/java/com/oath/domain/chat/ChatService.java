package com.oath.domain.chat;

import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 특정 그룹(채팅방)의 이전 대화 내용을 페이징하여 조회합니다.
     *
     * @param groupId   조회할 그룹의 ID
     * @param userEmail 요청한 사용자의 이메일 (권한 확인용)
     * @param pageable  페이징 정보
     * @return 페이징된 메시지 응답 DTO
     */
    public PageResponseDTO<ChatResponse> getPreviousMessages(
            Long groupId,
            String userEmail,
            Pageable pageable
    ) {
        log.info("이전 메시지 조회 요청: groupId={}, userEmail={}, page={}", groupId, userEmail, pageable.getPageNumber());

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 사용자가 해당 그룹의 멤버인지 권한을 검증합니다.
        if (!groupMemberRepository.existsByGroupIdAndMemberId(groupId, member.getId())) {
            throw new Exception403("해당 그룹의 대화 내용을 볼 권한이 없습니다.");
        }

        Page<Chat> messagesPage = chatRepository.findByGroupIdOrderBySentAtDesc(groupId, pageable);

        log.info("이전 메시지 조회 완료: groupId={}, 조회된 메시지 수={}", groupId, messagesPage.getNumberOfElements());

        return PageResponseDTO.from(
                messagesPage,
                chat -> ChatResponse.builder()
                        .messageId(chat.getId())
                        .senderId(chat.getSender().getId())
                        .senderName(chat.getSender().getUsername())
                        .senderProfileImageUrl(chat.getSender().getProfileImageUrl())
                        .content(chat.getContent())
                        .planId(chat.getPlanId())
                        .sentAt(chat.getSentAt())
                        .build(),
                5
        );
    }

    /**
     * WebSocket을 통해 수신된 채팅 메시지를 처리하고 저장합니다.
     */
    @Transactional
    public ChatResponse processAndSaveMessage(
            ChatRequest request,
            Long groupId,
            String senderEmail
    ) {
        log.info("WebSocket 메시지 처리 시작: groupId={}, senderEmail={}, content='{}'", groupId, senderEmail, request.getContent());

        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception404("그룹을 찾을 수 없습니다."));

        // 사용자가 해당 그룹의 멤버인지 권한을 검증합니다.
        if (!groupMemberRepository.existsByGroupIdAndMemberId(groupId, sender.getId())) {
            throw new Exception403("해당 그룹에 메시지를 보낼 권한이 없습니다.");
        }

        Chat chat = Chat.of(group, sender, request.getContent(), request.getPlanId());
        chatRepository.save(chat);

        log.info("메시지 저장 완료: messageId={}, content='{}'", chat.getId(), chat.getContent());

        return ChatResponse.from(chat);
    }
}