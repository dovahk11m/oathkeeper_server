package com.oath.domain.groups.groupController;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chats.ChatResponse;
import com.oath.domain.chats.ChatService;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupDTO.GroupMemberResponse;
import com.oath.domain.groups.groupDTO.GroupMembersAddRequest;
import com.oath.domain.groups.groupService.GroupService;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.facade.PlanFacade;
import com.oath.domain.plan.request.PlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Group API", description = "그룹 및 채팅 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final ChatService chatService;
    private final PlanFacade planFacade;
    private final MemberRepository memberRepository;

    @Auth
    @Operation(summary = "새로운 그룹 생성", description = "새로운 그룹을 생성합니다. 그룹 생성 시, 해당 그룹의 채팅 기능도 함께 활성화됩니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createGroup(
            @Parameter(description = "그룹 생성 요청 정보", required = true) @RequestBody GroupCreateRequest request,
            @RequestAttribute("userEmail") String email
    ) {
        Group newGroup = groupService.createGroup(
                request,
                email
        );
        return new ResponseEntity<>(
                CommonResponse.success(
                        newGroup.getId(),
                        "그룹이 생성되었습니다."
                ),
                HttpStatus.CREATED
        );
    }

    @Auth
    @Operation(summary = "사용자가 참여중인 그룹 목록 조회", description = "현재 로그인한 사용자가 참여하고 있는 모든 그룹의 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponseDTO<GroupListResponse>>> getMyGroups(
            @RequestAttribute("userEmail") String email,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDTO<GroupListResponse> myGroups = groupService.getMyGroups(
                email,
                pageable
        );
        return ResponseEntity.ok(CommonResponse.success(
                myGroups,
                "그룹 목록 조회가 완료되었습니다."
        ));
    }

    @Auth
    @Operation(summary = "그룹 내 약속 목록 조회", description = "특정 그룹에 속한 약속 목록을 페이징하여 조회합니다. status 파라미터로 약속 상태(예: COMPLETED)를 필터링할 수 있습니다.")
    @GetMapping("/{groupId}/plans")
    public ResponseEntity<CommonResponse<PageResponseDTO<PlanResponse.SimplePlan>>> getPlansByGroup(
            @Parameter(description = "약속 목록을 조회할 그룹의 ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "조회할 약속의 상태 (PLANNING, CONFIRMED, COMPLETED, CANCELLED)") @RequestParam(required = false) Status status,
            @RequestAttribute("userEmail") String email,
            @PageableDefault(size = 20, sort = "planDatetime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        groupService.validateGroupMember(groupId, member.getId());

        PageResponseDTO<PlanResponse.SimplePlan> plans = planFacade.getPlansByGroupAndStatus(groupId, status, pageable);
        return ResponseEntity.ok(CommonResponse.success(plans, "그룹 내 약속 목록 조회가 완료되었습니다."));
    }


    @Auth
    @Operation(summary = "그룹에서 탈퇴", description = "현재 로그인한 사용자가 특정 그룹에서 탈퇴합니다. 마지막 멤버가 탈퇴할 경우, 그룹과 모든 관련 데이터(채팅 내역 등)가 영구적으로 삭제됩니다.")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<CommonResponse<Void>> leaveGroup(
            @Parameter(description = "탈퇴할 그룹의 ID", required = true) @PathVariable Long groupId,
            @RequestAttribute("userEmail") String email
    ) {
        groupService.leaveGroup(
                groupId,
                email
        );
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "그룹에서 탈퇴했습니다."
        ));
    }

    @Auth
    @Operation(summary = "그룹에 멤버 추가", description = "특정 그룹에 한 명 이상의 새로운 멤버를 추가(초대)합니다. 그룹 멤버라면 누구나 다른 사람을 초대할 수 있습니다.")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<Void>> addMembers(
            @Parameter(description = "멤버를 추가할 그룹의 ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "추가할 멤버들의 이메일 목록", required = true) @RequestBody GroupMembersAddRequest request,
            @RequestAttribute("userEmail") String email
    ) {
        groupService.addMembers(
                groupId,
                request,
                email
        );
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "멤버가 그룹에 추가되었습니다."
        ));
    }


    @Auth
    @Operation(summary = "그룹(채팅방)의 이전 대화 내용 조회", description = "특정 그룹(채팅방)의 이전 대화 내용을 페이징하여 조회합니다. 메시지는 최신순으로 정렬됩니다.")
    @GetMapping("/{groupId}/chat/messages")
    public ResponseEntity<CommonResponse<PageResponseDTO<ChatResponse>>> getPreviousMessages(
            @Parameter(description = "대화 내용을 조회할 그룹의 ID", required = true) @PathVariable Long groupId,
            @RequestAttribute("userEmail") String email,
            @PageableDefault(size = 30, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDTO<ChatResponse> messages = chatService.getPreviousMessages(
                groupId,
                email,
                pageable
        );
        return ResponseEntity.ok(CommonResponse.success(
                messages,
                "이전 대화 내용 조회가 완료되었습니다."
        ));
    }

    @Auth
    @Operation(summary = "그룹 멤버 목록 조회", description = "특정 그룹에 참여하고 있는 모든 멤버의 목록을 페이징하여 조회합니다.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<PageResponseDTO<GroupMemberResponse>>> getGroupMembers(
            @Parameter(description = "멤버 목록을 조회할 그룹의 ID", required = true) @PathVariable Long groupId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDTO<GroupMemberResponse> members = groupService.getGroupMembers(
                groupId,
                pageable
        );
        return ResponseEntity.ok(CommonResponse.success(
                members,
                "그룹 멤버 목록 조회가 완료되었습니다."
        ));
    }
}
