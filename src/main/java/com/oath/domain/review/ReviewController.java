package com.oath.domain.review;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception401;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review API", description = "약속 후기 관련 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private final MemberRepository memberRepository;

    private Member getCurrentMember(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception401("사용자를 찾을 수 없습니다."));
    }

    // 후기 생성
    @Auth
    @Operation(summary = "후기 작성", description = "약속에 대한 후기를 작성합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<ReviewResponse.ReviewDTO>> createReview(
            @Valid @RequestBody ReviewRequest.CreateReviewRequest request,
            HttpServletRequest httpRequest) {
        Member member = getCurrentMember(httpRequest);

        Review review = reviewService.createReview(
                member.getId(),
                request.getPlanId(),
                request.getTitle(),
                request.getContent()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(ReviewResponse.ReviewDTO.of(review), "후기가 작성되었습니다."));
    }

    // 후기 단건 조회
    @Operation(summary = "후기 조회", description = "후기 상세 내용을 조회합니다.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewResponse.ReviewDTO>> getReview(@PathVariable Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(CommonResponse.success(ReviewResponse.ReviewDTO.of(review)));
    }

    // Plan별 후기 목록 조회
    @Operation(summary = "약속별 후기 목록", description = "특정 약속에 대한 모든 후기를 조회합니다.")
    @GetMapping("/plan/{planId}")
    public ResponseEntity<CommonResponse<List<ReviewResponse.ReviewDTO>>> getReviewsByPlan(@PathVariable Long planId) {
        List<ReviewResponse.ReviewDTO> dtos = reviewService.getReviewsByPlanId(planId).stream()
                .map(ReviewResponse.ReviewDTO::of)
                .toList();
        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 작성자별 후기 목록 조회
    @Auth
    @Operation(summary = "내가 작성한 후기 목록", description = "로그인한 사용자가 작성한 모든 후기를 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<Page<ReviewResponse.ReviewDTO>>> getMyReviews(
            HttpServletRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        Member member = getCurrentMember(request);

        Page<Review> reviews = reviewService.getReviewsByAuthorId(member.getId(), pageable);
        Page<ReviewResponse.ReviewDTO> dtos = reviews.map(ReviewResponse.ReviewDTO::of);

        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 후기 수정
    @Auth
    @Operation(summary = "후기 수정", description = "작성한 후기를 수정합니다.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewResponse.ReviewDTO>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest.UpdateReviewRequest request,
            HttpServletRequest httpRequest) {
        Member member = getCurrentMember(httpRequest);

        Review review = reviewService.updateReview(
                reviewId,
                member.getId(),
                request.getTitle(),
                request.getContent()
        );

        return ResponseEntity.ok(CommonResponse.success(ReviewResponse.ReviewDTO.of(review), "후기가 수정되었습니다."));
    }

    // 후기 삭제
    @Auth
    @Operation(summary = "후기 삭제", description = "작성한 후기를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<CommonResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        Member member = getCurrentMember(request);
        reviewService.deleteReview(reviewId, member.getId());
        return ResponseEntity.ok(CommonResponse.success(null, "후기가 삭제되었습니다."));
    }
}
