package com.oath.domain.groups.groupService;

import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupDTO.GroupMemberResponse;
import com.oath.domain.groups.groupDTO.GroupMembersAddRequest;
import com.oath.domain.groups.groupEvent.CreateGroupEvent;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(value = "h2TransactionManager", readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Group createGroup(
            GroupCreateRequest request,
            String creatorEmail
    ) {
        Member creator = memberRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 1. DTO로부터 Group 엔티티 생성 및 저장
        Group newGroup = Group.from(request);
        groupRepository.save(newGroup);

        // 2. 생성자를 OWNER로 하는 그룹 멤버 생성 및 저장
        GroupMember ownerMember = GroupMember.of(
                newGroup,
                creator
        );
        groupMemberRepository.save(ownerMember);

        // 3.완료되면 변경사실 전파
        eventPublisher.publishEvent(new CreateGroupEvent(newGroup, creator));
        return newGroup;
    }

    public PageResponseDTO<GroupListResponse> getMyGroups(
            String email,
            Pageable pageable
    ) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        Page<GroupMember> myGroupsPage = groupMemberRepository.findByMemberId(
                member.getId(),
                pageable
        );

        return PageResponseDTO.from(
                myGroupsPage,
                groupMember -> {
                    Group group = groupMember.getGroup();

                    // 마지막 메시지 정보 조회
                    Optional<Chat> lastMessageOpt = chatRepository.findTopByGroupIdOrderBySentAtDesc(group.getId());

                    // TODO: 안 읽은 메시지 수 조회 로직 구현 필요
                    Long unreadCount = 0L;

                    return GroupListResponse.builder()
                            .groupId(group.getId())
                            .groupName(group.getName())
                            .chatRoomId(group.getId()) // ChatRoomID는 이제 GroupID와 동일
                            .lastMessage(lastMessageOpt.map(Chat::getContent)
                                                 .orElse("대화 내용이 없습니다."))
                            .lastMessageSentAt(lastMessageOpt.map(Chat::getSentAt)
                                                       .orElse(null))
                            .unreadCount(unreadCount)
                            .build();
                },
                5
        );
    }

    /**
     * 그룹 ID로 그룹을 조회합니다.
     * @param groupId 조회할 그룹 ID
     * @return 조회된 Group 엔티티
     * @throws Exception404 그룹을 찾을 수 없을 경우
     */
    public Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception404("그룹을 찾을 수 없습니다."));
    }

    /**
     * 사용자가 특정 그룹의 멤버인지 확인합니다. 멤버가 아닐 경우 403 예외를 발생시킵니다.
     * @param groupId 확인할 그룹 ID
     * @param memberId 확인할 멤버 ID
     */
    public void validateGroupMember(Long groupId, Long memberId) {
        if (!groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
            throw new Exception403("해당 그룹의 멤버가 아닙니다.");
        }
    }

    /**
     * 특정 그룹의 멤버 목록을 페이징하여 조회합니다.
     * (채팅방 참여자 목록과 동일한 의미)
     */
    public PageResponseDTO<GroupMemberResponse> getGroupMembers(
            Long groupId,
            Pageable pageable
    ) {
        // TODO: 요청자가 해당 그룹의 멤버인지 확인하는 권한 검증 로직 추가 필요

        Page<GroupMember> membersPage = groupMemberRepository.findByGroupIdWithMember(
                groupId,
                pageable
        );

        return PageResponseDTO.from(
                membersPage,
                groupMember -> {
                    Member member = groupMember.getMember();
                    return GroupMemberResponse.builder()
                            .memberId(member.getId())
                            .username(member.getUsername())
                            .profileImageUrl(member.getProfileImageUrl())
                            .build();
                },
                5
        );
    }

    /**
     * 특정 그룹에 여러 멤버를 한 번에 추가합니다.
     *
     * @param groupId        그룹 ID
     * @param request        추가할 멤버들의 이메일 목록을 담은 DTO
     * @param requesterEmail 요청을 보낸 사용자의 이메일 (권한 확인용)
     */
    @Transactional
    public void addMembers(
            Long groupId,
            GroupMembersAddRequest request,
            String requesterEmail
    ) {
        // 1. 요청자 및 그룹 정보 조회
        Member requester = memberRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new Exception404("요청자 정보를 찾을 수 없습니다."));
        groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception404("그룹을 찾을 수 없습니다."));

        // 2. 요청자가 그룹의 멤버인지 권한을 검증합니다.
        validateGroupMember(groupId, requester.getId());

        // 3. DB에서 추가할 멤버 목록을 한 번에 조회
        List<Member> membersToAdd = memberRepository.findByEmailIn(request.getMemberEmails());

        // 4. 이미 그룹에 속한 멤버 ID 목록을 한 번에 조회하여 필터링
        List<Long> memberIdsToAdd = membersToAdd.stream()
                .map(Member::getId)
                .toList();
        List<Long> existingMemberIds = groupMemberRepository.findByGroupIdAndMemberIdIn(
                        groupId,
                        memberIdsToAdd
                )
                .stream()
                .map(gm -> gm.getMember()
                        .getId())
                .toList();

        // 5. 새로운 멤버들만 GroupMember 객체로 만들어 저장
        List<GroupMember> newGroupMembers = membersToAdd.stream()
                .filter(member -> !existingMemberIds.contains(member.getId()))
                .map(member -> GroupMember.of(
                        groupRepository.getReferenceById(groupId), // 프록시 사용
                        member
                ))
                .toList();

        groupMemberRepository.saveAll(newGroupMembers);
    }

    @Transactional
    public void leaveGroup(
            Long groupId,
            String userEmail
    ) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(
                        groupId,
                        member.getId()
                )
                .orElseThrow(() -> new Exception403("해당 그룹의 멤버가 아닙니다."));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        // [수정] 오너 구분 없이, 마지막 멤버가 탈퇴하면 그룹을 삭제하는 로직으로 단순화
        if (memberCount <= 1) {
            // 마지막 멤버가 탈퇴하는 경우, 그룹 전체 삭제
            groupRepository.delete(groupMember.getGroup());
        } else {
            // 다른 멤버가 남아있으면, 자신의 멤버십 정보만 삭제
            groupMemberRepository.delete(groupMember);
        }
    }
}
