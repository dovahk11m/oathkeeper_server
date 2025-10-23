package com.oath.domain.chat;

import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final GroupRepository groupRepository; // ChatRoomRepository 대신 사용
    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;

    /**
     * 특정 채팅방의 이전 대화 내용을 페이징하여 조회합니다.
     */
    public PageResponseDTO<ChatResponse> getPreviousMessages(
            Long groupId,
            // 파라미터를 roomId 대신 groupId로 변경
            Pageable pageable
    ) {
        Page<Chat> messagesPage = chatRepository.findByGroupIdOrderBySentAtDesc(
                groupId,
                pageable
        );

        return PageResponseDTO.from(
                messagesPage,
                chatMessage -> ChatResponse.builder()
                        .messageId(chatMessage.getId())
                        .senderId(chatMessage.getSender()
                                          .getId())
                        .senderName(chatMessage.getSender()
                                            .getUsername())
                        .senderProfileImageUrl(chatMessage.getSender()
                                                       .getProfileImageUrl())
                        .content(chatMessage.getContent())
                        .planId(chatMessage.getPlanId())
                        .sentAt(chatMessage.getSentAt())
                        .build(),
                5
                // 페이지네이션 바에 5개씩 표시
        );
    }

    //채팅 메시지 저장 및 브로드캐스팅
    @Transactional
    public ChatResponse processAndSaveMessage(
            ChatRequest request,
            Long groupId,
            // 파라미터를 roomId 대신 groupId로 변경
            String senderEmail
    ) {
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception404("그룹을 찾을 수 없습니다."));

        // TODO: sender가 해당 roomId의 멤버인지 확인하는 검증 로직 추가

        Chat chat = Chat.builder()
                .group(group)
                .sender(sender)
                .content(request.getContent())
                .planId(request.getPlanId())
                .sentAt(LocalDateTime.now())
                .build();

        chatRepository.save(chat);

        return ChatResponse.builder()
                .messageId(chat.getId())
                .senderId(sender.getId())
                .senderName(sender.getUsername())
                .senderProfileImageUrl(sender.getProfileImageUrl())
                .content(chat.getContent())
                .planId(chat.getPlanId())
                .sentAt(chat.getSentAt())
                .build();
    }
}