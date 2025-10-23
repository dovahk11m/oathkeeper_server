package com.oath.domain.groups.groupService;

import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chat.ChatMessage;
import com.oath.domain.chat.ChatRoom;
import com.oath.domain.chat.chatRepository.ChatMessageRepository;
import com.oath.domain.chat.chatRepository.ChatRoomRepository;
import com.oath.domain.chat.chatService.ChatService;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.GroupRole;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;

    @Transactional
    public Group createGroup(GroupCreateRequest request, String creatorEmail) {
        Member creator = memberRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 1. DTO로부터 Group 엔티티 생성 및 저장
        Group newGroup = Group.from(request);
        groupRepository.save(newGroup);

        // 2. 생성자를 OWNER로 하는 그룹 멤버 생성 및 저장
        GroupMember ownerMember = GroupMember.of(newGroup, creator, GroupRole.OWNER);
        groupMemberRepository.save(ownerMember);

        // 3. 그룹과 1:1 매칭되는 채팅방 생성
        ChatRoom newChatRoom = chatService.createChatRoomForGroup(newGroup);

        // 4. Group과 ChatRoom의 양방향 연관관계 설정
        newGroup.setChatRoom(newChatRoom);

        return newGroup;
    }

    public PageResponseDTO<GroupListResponse> getMyGroups(String email, Pageable pageable) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        Page<GroupMember> myGroupsPage = groupMemberRepository.findByMemberIdWithGroup(member.getId(), pageable);

        return PageResponseDTO.from(myGroupsPage, groupMember -> {
            Group group = groupMember.getGroup();
            ChatRoom chatRoom = group.getChatRoom();

            // 마지막 메시지 정보 조회
            Optional<ChatMessage> lastMessageOpt = Optional.empty();
            if (chatRoom != null) {
                lastMessageOpt = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId());
            }

            // TODO: 안 읽은 메시지 수 조회 로직 구현 필요
            Long unreadCount = 0L;

            return GroupListResponse.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .chatRoomId(chatRoom != null ? chatRoom.getId() : null)
                    .lastMessage(lastMessageOpt.map(ChatMessage::getContent).orElse("대화 내용이 없습니다."))
                    .lastMessageSentAt(lastMessageOpt.map(ChatMessage::getSentAt).orElse(null))
                    .unreadCount(unreadCount)
                    .build();
        }, 5);
    }

    @Transactional
    public void leaveGroup(Long groupId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, member.getId())
                .orElseThrow(() -> new Exception403("해당 그룹의 멤버가 아닙니다."));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        if (memberCount > 1) {
            // 다른 멤버가 남아있으면, 그냥 탈퇴 처리
            groupMemberRepository.delete(groupMember);
        } else {
            // 마지막 멤버가 탈퇴하는 경우, 그룹과 관련 데이터 모두 삭제
            // Group 엔티티의 cascade 설정에 의해 ChatRoom도 함께 삭제됩니다.
            groupRepository.deleteById(groupId);
        }
    }
}