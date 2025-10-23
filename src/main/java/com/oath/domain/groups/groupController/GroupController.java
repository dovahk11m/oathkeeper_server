package com.oath.domain.groups.groupController;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.paging.PageResponseDTO; 
import com.oath.domain.groups.groupDTO.GroupMembersAddRequest;
import com.oath.domain.groups.groupDTO.GroupMemberResponse;
import com.oath.domain.chat.ChatResponse;
import com.oath.domain.chat.ChatService;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupService.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final ChatService chatService; // Chat 관련 기능 처리를 위해 주입

    /**
     * 새로운 그룹을 생성합니다.
     * 그룹 생성 시, 1:1로 매칭되는 채팅방도 함께 생성됩니다.
     */
    @Auth
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createGroup(
            @RequestBody GroupCreateRequest request,
            @RequestAttribute("userEmail") String email
    ) {
        Group newGroup = groupService.createGroup(request, email);
        return new ResponseEntity<>(CommonResponse.success(newGroup.getId(), "그룹이 생성되었습니다."), HttpStatus.CREATED);
    }

    /**
     * 현재 로그인한 사용자가 참여중인 그룹 목록을 조회합니다.
     * 각 그룹 정보에는 1:1 매칭된 채팅방의 마지막 메시지 정보가 포함됩니다.
     */
    @Auth
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponseDTO<GroupListResponse>>> getMyGroups(
            @RequestAttribute("userEmail") String email,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDTO<GroupListResponse> myGroups = groupService.getMyGroups(email, pageable);
        return ResponseEntity.ok(CommonResponse.success(myGroups, "그룹 목록 조회가 완료되었습니다."));
    }

    /**
     * 특정 그룹에서 탈퇴합니다.
     * 마지막 멤버가 탈퇴할 경우, 그룹과 관련 데이터(채팅방 등)가 모두 삭제됩니다.
     */
    @Auth
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<CommonResponse<Void>> leaveGroup(
            @PathVariable Long groupId,
            @RequestAttribute("userEmail") String email
    ) {
        groupService.leaveGroup(groupId, email);
        return ResponseEntity.ok(CommonResponse.success(null, "그룹에서 탈퇴했습니다."));
    }

    /**
     * 특정 그룹에 여러 멤버를 추가합니다.
     */
    @Auth
    @PostMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<Void>> addMembers(
            @PathVariable Long groupId,
            @RequestBody GroupMembersAddRequest request,
            @RequestAttribute("userEmail") String email
    ) {
        groupService.addMembers(groupId, request, email);
        return ResponseEntity.ok(CommonResponse.success(null, "멤버가 그룹에 추가되었습니다."));
    }


    /**
     * 특정 그룹(채팅방)의 이전 대화 내용을 페이징하여 조회합니다.
     */
    @Auth
    @GetMapping("/{groupId}/chat/messages")
    public ResponseEntity<CommonResponse<PageResponseDTO<ChatResponse>>> getPreviousMessages(
            @PathVariable Long groupId,
            // TODO: ChatRoom ID를 어떻게 가져올지 결정 필요 (GroupRepository 사용 등)
            @PageableDefault(size = 30, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 임시로 roomId를 groupId로 사용. 실제로는 groupId로 chatRoomId를 조회해야 함.
        Long roomId = groupId;
        PageResponseDTO<ChatResponse> messages = chatService.getPreviousMessages(roomId, pageable);
        return ResponseEntity.ok(CommonResponse.success(messages, "이전 대화 내용 조회가 완료되었습니다."));
    }

    /**
     * 특정 그룹의 멤버 목록(채팅방 참여자 목록)을 조회합니다.
     */
    @Auth
    @GetMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<PageResponseDTO<GroupMemberResponse>>> getGroupMembers(
            @PathVariable Long groupId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDTO<GroupMemberResponse> members = groupService.getGroupMembers(groupId, pageable);
        return ResponseEntity.ok(CommonResponse.success(members, "그룹 멤버 목록 조회가 완료되었습니다."));
    }
}