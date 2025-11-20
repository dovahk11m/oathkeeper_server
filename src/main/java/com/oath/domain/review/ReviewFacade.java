package com.oath.domain.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;

    @Transactional(readOnly = true)
    public ReviewResponse.ReviewDTO getReviewDetail(Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ReviewResponse.ReviewDTO.of(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse.ReviewDTO> getReviewsByPlan(Long planId) {
        List<Review> reviews = reviewService.getReviewsByPlanId(planId);
        return reviews.stream()
                .map(ReviewResponse.ReviewDTO::of)
                .toList();
    }
}

