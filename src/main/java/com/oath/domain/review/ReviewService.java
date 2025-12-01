package com.oath.domain.review;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewJpaRepository reviewJpaRepository;
    private final PlanJpaRepository planJpaRepository;
    private final MemberRepository memberRepository;

    // 후기 조회
    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        return reviewJpaRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new Exception404("해당 후기를 찾을 수 없습니다."));
    }

    // Plan별 후기 목록 조회
    @Transactional(readOnly = true)
    public List<Review> getReviewsByPlanId(Long planId) {
        return reviewJpaRepository.findByPlanIdWithDetails(planId);
    }

    // 작성자별 후기 목록 조회
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByAuthorId(Long authorId, Pageable pageable) {
        return reviewJpaRepository.findByAuthorIdWithDetails(authorId, pageable);
    }

    // 후기 생성 - (약속이 종료된 상태에서 작성 가능)
    @Transactional
    public Review createReview(Long memberId, Long planId, String title, String content) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));

        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));

        // 약속이 완료되었는지 확인
        if (plan.getStatus() != Status.COMPLETED) {
            throw new Exception400("약속이 종료된 후에만 후기를 작성할 수 있습니다.");
        }

        // 이미 후기를 작성했는지 확인
        if (reviewJpaRepository.existsByPlanIdAndAuthorId(planId, memberId)) {
            throw new Exception400("이미 해당 약속에 대한 후기를 작성하셨습니다.");
        }

        Review review = Review.builder()
                .author(member)
                .plan(plan)
                .title(title)
                .content(content)
                .build();

        return reviewJpaRepository.save(review);
    }

    // 후기 수정
    @Transactional
    public Review updateReview(Long reviewId, Long requesterId, String title, String content) {
        Review review = getReviewById(reviewId);

        // 작성자 본인만 수정 가능
        validateAuthor(review, requesterId);

        review.update(title, content);
        return reviewJpaRepository.save(review);
    }

    // 후기 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long requesterId) {
        Review review = getReviewById(reviewId);

        // 작성자 본인만 삭제 가능
        validateAuthor(review, requesterId);

        reviewJpaRepository.delete(review);
    }

    // 작성자 권한 검증
    private void validateAuthor(Review review, Long memberId) {
        if (!review.getAuthor().getId().equals(memberId)) {
            throw new Exception403("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}
