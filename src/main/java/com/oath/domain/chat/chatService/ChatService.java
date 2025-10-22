package com.oath.domain.chat.chatService;

import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chat.ChatMessage;
import com.oath.domain.chat.ChatRoom;
import com.oath.domain.chat.ChatRoomMember;
import com.oath.domain.chat.chatDTO.MessageRequest;
import com.oath.domain.chat.chatDTO.MessageResponse;
import com.oath.domain.chat.chatDTO.RoomCreateRequest;
import com.oath.domain.chat.chatDTO.RoomListResponse;
import com.oath.domain.chat.chatRepository.ChatMessageRepository;
import com.oath.domain.chat.chatRepository.ChatRoomMemberRepository;
import com.oath.domain.chat.chatRepository.ChatRoomRepository;
import com.oath.domain.members.domain.Member;
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

    //채팅방 생성 로직
    @Transactional
    public ChatRoom createChatRoom(
            RoomCreateRequest request,
            String creatorEmail
    ) {
        Member creator = memberRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 1. 채팅방 생성
        ChatRoom newChatRoom = ChatRoom.builder()
                .groupId(request.getGroupId())
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(newChatRoom);

        // 2. 생성자를 채팅방 멤버로 추가
        ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                .chatRoom(newChatRoom)
                .member(creator)
                .joinedAt(LocalDateTime.now())
                .build();
        chatRoomMemberRepository.save(chatRoomMember);

        // TODO: 그룹의 다른 멤버들도 ChatRoomMember로 추가하는 로직 필요

        return newChatRoom;
    }

    //채팅 목록조회 로직
    public PageResponseDTO<RoomListResponse> getMyChatRooms(
            String email,
            Pageable pageable
    ) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        Page<ChatRoomMember> myRoomsPage = chatRoomMemberRepository.findByMemberIdWithChatRoom(
                member.getId(),
                pageable
        );

        // Page<ChatRoomMember>를 PageResponseDTO<RoomListResponse>로 변환
        return PageResponseDTO.from(
                myRoomsPage,
                chatRoomMember -> {
                    ChatRoom chatRoom = chatRoomMember.getChatRoom();
                    // TODO: 각 채팅방의 마지막 메시지 및 안 읽은 메시지 수 조회 로직 구현
                    String lastMessage = "대화 내용이 없습니다.";
                    Long unreadCount = 0L;

                    return RoomListResponse.builder()
                            .chatRoomId(chatRoom.getId())
                            .chatRoomName(chatRoom.getName())
                            .lastMessage(lastMessage)
                            .unreadCount(unreadCount)
                            .build();
                },
                5
        ); // 페이지네이션 바에 5개씩 표시
    }

    /**
     * 채팅 메시지를 DB에 저장하고, 브로드캐스팅을 위해 DTO로 변환하여 반환합니다.
     */
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