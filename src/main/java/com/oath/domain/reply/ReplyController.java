package com.oath.domain.reply;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception401;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.review.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reply API", description = "후기 댓글 관련 API")
@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final MemberRepository memberRepository;

    private Member getCurrentMember(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception401("사용자를 찾을 수 없습니다."));
    }

    // 댓글 작성
    @Auth
    @Operation(summary = "댓글 작성", description = "후기에 댓글을 작성합니다.")
    @PostMapping("/review/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewResponse.ReplyDTO>> createReply(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReplyRequest request,
            HttpServletRequest httpRequest) {
        Member member = getCurrentMember(httpRequest);
        Reply reply = replyService.createReply(member.getId(), reviewId, request.getContent());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(ReviewResponse.ReplyDTO.of(reply), "댓글이 작성되었습니다."));
    }

    // 댓글 수정
    @Auth
    @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
    @PutMapping("/{replyId}")
    public ResponseEntity<CommonResponse<ReviewResponse.ReplyDTO>> updateReply(
            @PathVariable Long replyId,
            @Valid @RequestBody ReplyRequest request,
            HttpServletRequest httpRequest) {
        Member member = getCurrentMember(httpRequest);
        Reply reply = replyService.updateReply(replyId, member.getId(), request.getContent());
        return ResponseEntity.ok(CommonResponse.success(ReviewResponse.ReplyDTO.of(reply), "댓글이 수정되었습니다."));
    }

    // 댓글 삭제
    @Auth
    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @DeleteMapping("/{replyId}")
    public ResponseEntity<CommonResponse<Void>> deleteReply(
            @PathVariable Long replyId,
            HttpServletRequest request) {
        Member member = getCurrentMember(request);
        replyService.deleteReply(replyId, member.getId());
        return ResponseEntity.ok(CommonResponse.success(null, "댓글이 삭제되었습니다."));
    }
}

