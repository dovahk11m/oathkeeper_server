package com.oath.domain.chat.chatService;

import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chat.ChatMessage;
import com.oath.domain.chat.ChatRoom;
import com.oath.domain.groups.Group;
import com.oath.domain.chat.ChatRoomMember;
import com.oath.domain.chat.chatDTO.*;
import com.oath.domain.chat.chatRepository.ChatMessageRepository;
import com.oath.domain.chat.chatRepository.ChatRoomMemberRepository;
import com.oath.domain.chat.chatRepository.ChatRoomRepository;
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

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;

    /**
     * 특정 그룹에 종속되는 1:1 채팅방을 생성합니다.
     * 이 메서드는 GroupService에 의해 호출되는 것을 가정합니다.
     * @param group 이 채팅방이 속하게 될 Group 엔티티
     * @return 생성된 ChatRoom 엔티티
     */
    @Transactional
    public ChatRoom createChatRoomForGroup(Group group) {
        ChatRoom newChatRoom = ChatRoom.builder()
                .group(group)
                .name(group.getName() + " 채팅방") // 그룹 이름 기반으로 채팅방 이름 자동 생성
                .group(group) // 연관관계의 주인인 ChatRoom에 Group을 설정
                .createdAt(LocalDateTime.now())
                .build();
        return chatRoomRepository.save(newChatRoom);
    }

    /**
     * 특정 채팅방의 이전 대화 내용을 페이징하여 조회합니다.
     */
    public PageResponseDTO<MessageResponse> getPreviousMessages(
            Long roomId,
            Pageable pageable
    ) {
        Page<ChatMessage> messagesPage = chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(
                roomId,
                pageable
        );

        return PageResponseDTO.from(
                messagesPage,
                chatMessage -> MessageResponse.builder()
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

    /**
     * 특정 채팅방에 참여하고 있는 멤버 목록을 조회합니다.
     */
    public PageResponseDTO<MemberResponse> getChatRoomMembers(
            Long roomId,
            Pageable pageable
    ) {
        Page<ChatRoomMember> membersPage = chatRoomMemberRepository.findByChatRoomId(
                roomId,
                pageable
        );

        return PageResponseDTO.from(
                membersPage,
                chatRoomMember -> {
                    Member member = chatRoomMember.getMember();
                    return MemberResponse.builder()
                            .memberId(member.getId())
                            .username(member.getUsername())
                            .profileImageUrl(member.getProfileImageUrl())
                            .build();
                },
                5
                // 페이지네이션 바에 5개씩 표시
        );
    }

    //채팅 메시지 저장 및 브로드캐스팅
    @Transactional
    public MessageResponse processAndSaveMessage(
            MessageRequest request,
            Long roomId,
            String senderEmail
    ) {
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new Exception404("채팅방을 찾을 수 없습니다."));

        // TODO: sender가 해당 roomId의 멤버인지 확인하는 검증 로직 추가

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .planId(request.getPlanId())
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);

        return MessageResponse.builder()
                .messageId(chatMessage.getId())
                .senderId(sender.getId())
                .senderName(sender.getUsername())
                .senderProfileImageUrl(sender.getProfileImageUrl())
                .content(chatMessage.getContent())
                .planId(chatMessage.getPlanId())
                .sentAt(chatMessage.getSentAt())
                .build();
    }
}