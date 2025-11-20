package com.oath.domain.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class ReviewRequest {

    @Getter
    @Setter
    public static class CreateReviewRequest {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 500, message = "내용은 500자 이내여야 합니다.")
        private String content;

        @NotNull(message = "플랜 ID는 필수입니다.")
        private Long planId;
    }

    @Getter
    @Setter
    public static class UpdateReviewRequest {
        @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
        private String title;

        @Size(max = 500, message = "내용은 500자 이내여야 합니다.")
        private String content;
    }
}
