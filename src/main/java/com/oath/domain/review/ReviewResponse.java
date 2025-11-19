package com.oath.domain.review;

import com.oath.domain.reply.Reply;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewResponse {

    @Getter
    @NoArgsConstructor
    public static class ReviewDTO {
        private Long id;
        private String title;
        private String content;
        private Long planId;
        private String planTitle;
        private String authorName;
        private Long authorId;
        private String createdAt;
        private String updatedAt;
        private List<ReplyDTO> replies;

        public ReviewDTO(Review review) {
            if (review == null) return;

            this.id = review.getId();
            this.title = review.getTitle();
            this.content = review.getContent();
            this.planId = review.getPlan().getId();
            this.planTitle = review.getPlan().getTitle();
            this.authorName = review.getAuthor().getUsername();
            this.authorId = review.getAuthor().getId();
            this.createdAt = review.getCreatedAt() != null ? review.getCreatedAt().toString() : null;
            this.updatedAt = review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null;
            this.replies = review.getReplies().stream()
                    .map(ReplyDTO::new)
                    .collect(Collectors.toList());
        }

        public static ReviewDTO of(Review review) {
            return new ReviewDTO(review);
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ReplyDTO {
        private Long id;
        private String content;
        private String authorName;
        private Long authorId;
        private String createdAt;
        private String updatedAt;

        public ReplyDTO(Reply reply) {
            if (reply == null) return;

            this.id = reply.getId();
            this.content = reply.getContent();
            this.authorName = reply.getAuthor().getUsername();
            this.authorId = reply.getAuthor().getId();
            this.createdAt = reply.getCreatedAt() != null ? reply.getCreatedAt().toString() : null;
            this.updatedAt = reply.getUpdatedAt() != null ? reply.getUpdatedAt().toString() : null;
        }

        public static ReplyDTO of(Reply reply) {
            return new ReplyDTO(reply);
        }
    }
}

