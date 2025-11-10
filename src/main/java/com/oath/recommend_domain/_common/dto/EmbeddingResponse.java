package com.oath.recommend_domain._common.dto;

import java.util.Arrays;

public record EmbeddingResponse(
        Embedding embedding
) {
    public float[] getValues() {

        float[] embeddings = embedding.values;

        if (embeddings.length == 0) throw new NullPointerException("Gemini에서 빈 응답을 보냈습니다.");
        System.out.println(Arrays.toString(embeddings));

        return embeddings;
    }

    public record Embedding(
            float[] values
    ) {
    }
}

