package com.oath.domain.chat.chatController;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.paging.PageResponseDTO;
import com.oath.domain.chat.chatDTO.MemberResponse;
import com.oath.domain.chat.chatDTO.MessageResponse;
import com.oath.domain.chat.chatService.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 특정 채팅방의 이전 대화 내용을 페이징하여 조회합니다.
     */
    @Auth
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<PageResponseDTO<MessageResponse>>> getPreviousMessages(
            @PathVariable Long roomId,
            @PageableDefault(size = 30, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDTO<MessageResponse> messages = chatService.getPreviousMessages(roomId, pageable);
        return ResponseEntity.ok(CommonResponse.success(messages, "이전 대화 내용 조회가 완료되었습니다."));
    }

    /**
     * 특정 채팅방의 참여자 목록을 조회합니다.
     */
    @Auth
    @GetMapping("/{roomId}/members")
    public ResponseEntity<CommonResponse<PageResponseDTO<MemberResponse>>> getChatRoomMembers(
            @PathVariable Long roomId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDTO<MemberResponse> members = chatService.getChatRoomMembers(roomId, pageable);
        return ResponseEntity.ok(CommonResponse.success(members, "채팅방 참여자 목록 조회가 완료되었습니다."));
    }
}