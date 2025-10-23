package com.oath.domain.groups.groupController;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupService.GroupService;
import lombok.RequiredArgsConstructor;
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
}