package com.oath.domain.groups.groupController;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chat.ChatResponse;
import com.oath.domain.chat.ChatService;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupDTO.GroupListResponse;
import com.oath.domain.groups.groupDTO.GroupMemberResponse;
import com.oath.domain.groups.groupDTO.GroupMembersAddRequest;
import com.oath.domain.groups.groupService.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    //새로운 그룹 생성
    @Auth
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createGroup(
            @RequestBody GroupCreateRequest request,
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

    //사용자가 참여중인 그룹 목록 조회
    @Auth
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

    //그룹에서 탈퇴, 마지막 멤버라면 그룹과 데이터 모두 삭제
    @Auth
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<CommonResponse<Void>> leaveGroup(
            @PathVariable Long groupId,
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

    //그룹에 멤버 추가
    @Auth
    @PostMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<Void>> addMembers(
            @PathVariable Long groupId,
            @RequestBody GroupMembersAddRequest request,
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


    //그룹(채팅방)의 이전 대화 내용 조회
    @Auth
    @GetMapping("/{groupId}/chat/messages")
    public ResponseEntity<CommonResponse<PageResponseDTO<ChatResponse>>> getPreviousMessages(
            @PathVariable Long groupId,
            @RequestAttribute("userEmail") String email,
            // [수정] AuthInterceptor로부터 사용자 이메일 받기
            @PageableDefault(size = 30, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // [수정] ChatService에 userEmail을 함께 전달하여 권한 검증을 수행하도록 합니다.
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

    //그룹 멤버 목록 조회
    @Auth
    @GetMapping("/{groupId}/members")
    public ResponseEntity<CommonResponse<PageResponseDTO<GroupMemberResponse>>> getGroupMembers(
            @PathVariable Long groupId,
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

/*
 Group & Chat API 명세 (클라이언트 개발자용)

인증: 모든 API는 HTTP Header에 `Authorization: Bearer <JWT_토큰>`을 포함해야 합니다.



 1. 그룹 생성
- Method: `POST`
- URL: `/api/groups`
- 설명: 새로운 그룹을 생성합니다. 그룹 생성 시, 해당 그룹의 채팅 기능도 함께 활성화됩니다.
- Request Body:
json
{
  "groupName": "새로운 스터디 그룹"
}

- Response: `201 Created`
json
{
  "success": true,
  "data": 2, // 생성된 그룹의 ID
  "message": "그룹이 생성되었습니다."
}




 2. 내 그룹 목록 조회
- Method: `GET`
- URL: `/api/groups?page=0&size=10`
- 설명: 현재 로그인한 사용자가 참여하고 있는 모든 그룹의 목록을 페이징하여 조회합니다.
- Query Parameters:
  - `page`: 조회할 페이지 번호 (0부터 시작)
  - `size`: 한 페이지에 보여줄 그룹 수
- Response: `200 OK`
  - `PageResponseDTO<GroupListResponse>` 형식의 페이징된 데이터 (자세한 구조는 `PageResponseDTO` 명세 참고)
  - 각 `GroupListResponse` 객체는 마지막 메시지, 안 읽은 메시지 수 등의 정보를 포함합니다.



 3. 그룹 멤버 추가
- Method: `POST`
- URL: `/api/groups/{groupId}/members`
- 설명: 특정 그룹에 한 명 이상의 새로운 멤버를 추가(초대)합니다. 그룹 멤버라면 누구나 다른 사람을 초대할 수 있습니다.
- Path Variable:
  - `groupId`: 멤버를 추가할 그룹의 ID
- Request Body:
json
{
  "memberEmails": ["user2@test.com", "admin@test.com"]
}

- Response: `200 OK`
json
{
  "success": true,
  "data": null,
  "message": "멤버가 그룹에 추가되었습니다."
}




 4. 그룹 탈퇴
- Method: `DELETE`
- URL: `/api/groups/{groupId}/leave`
- 설명: 현재 로그인한 사용자가 특정 그룹에서 탈퇴합니다. 마지막 멤버가 탈퇴할 경우, 그룹과 모든 관련 데이터(채팅 내역 등)가 영구적으로 삭제됩니다.
- Path Variable:
  - `groupId`: 탈퇴할 그룹의 ID
- Response: `200 OK`
json
{
  "success": true,
  "data": null,
  "message": "그룹에서 탈퇴했습니다."
}




 5. 그룹 멤버 목록 조회
- Method: `GET`
- URL: `/api/groups/{groupId}/members?page=0&size=20`
- 설명: 특정 그룹에 참여하고 있는 모든 멤버의 목록을 페이징하여 조회합니다. (채팅방 참여자 목록과 동일)
- Path Variable:
  - `groupId`: 조회할 그룹의 ID
- Query Parameters:
  - `page`, `size`
- Response: `200 OK`
  - `PageResponseDTO<GroupMemberResponse>` 형식의 페이징된 데이터



 6. 이전 대화 내용 조회
- Method: `GET`
- URL: `/api/groups/{groupId}/chat/messages?page=0&size=30`
- 설명: 특정 그룹(채팅방)의 이전 대화 내용을 페이징하여 조회합니다. 메시지는 최신순으로 정렬됩니다.
- Path Variable:
  - `groupId`: 조회할 그룹의 ID
- Query Parameters:
  - `page`, `size`
- Response: `200 OK`
  - `PageResponseDTO<ChatResponse>` 형식의 페이징된 데이터

*/