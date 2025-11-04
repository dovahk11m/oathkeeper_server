package com.oath.recommend_domain.plan.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oath.recommend_domain.plan.constants.TaskType;

import java.util.List;

public record EmbeddingRequest(
        String model,
        Content content,

        @JsonProperty("output_dimensionality")
        int outputDimensionality,

        @JsonProperty("task_type")
        String taskType
) {
    // EmbeddingRequest를 생성하는 헬퍼메서드
    public static EmbeddingRequest buildEmbeddingRequest(String naturalLanguage, String embeddingModel) {
        // 1. Part 객체 생성
        EmbeddingRequest.Part part = new EmbeddingRequest.Part(naturalLanguage);

        // 2. Content 객체 생성
        EmbeddingRequest.Content content = new EmbeddingRequest.Content(List.of(part));

        // 3. 최종 Request 객체 생성 및 return
        return new EmbeddingRequest(embeddingModel, content, 768, TaskType.SEMANTIC_SIMILARITY.name());
    }

    public record Content(
            List<Part> parts
    ) {
    }

    public record Part(
            String text
    ) {
    }
}