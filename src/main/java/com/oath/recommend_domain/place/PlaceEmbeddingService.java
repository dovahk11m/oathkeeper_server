package com.oath.recommend_domain.place;

import com.oath.common.exception.Exception404;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.recommend_domain._common.dto.EmbeddingRequest;
import com.oath.recommend_domain._common.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PlaceEmbeddingService {

    private final PlaceEmbeddingRepository placeEmbeddingRepository;
    private final PlaceRepository placeRepository;
    private final String apiKey;
    private final String embeddingEndpoint;
    private final String embeddingModel;

    public PlaceEmbeddingService(PlaceEmbeddingRepository placeEmbeddingRepository,
                                 PlaceRepository placeRepository,
                                 @Value("${ai.gemini.api-key}") String apiKey,
                                 @Value("${ai.gemini.embedding-endpoint}") String embeddingEndpoint,
                                 @Value("${ai.gemini.embedding-model}") String embeddingModel) {

        this.placeEmbeddingRepository = placeEmbeddingRepository;
        this.placeRepository = placeRepository;
        this.apiKey = apiKey;
        this.embeddingEndpoint = embeddingEndpoint;
        this.embeddingModel = embeddingModel;

        if (apiKey.equals("FAKE_AI_KEY"))
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Gemini API Key가 할당되지 않아, 가짜 키가 주입되었습니다.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    // 임베딩 헬퍼 메서드
    private float[] getVector(String naturalLanguage) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(EmbeddingRequest.buildEmbeddingRequest(naturalLanguage, embeddingModel), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(embeddingEndpoint, HttpMethod.POST, entity, EmbeddingResponse.class);

        if (response.getBody() == null)
            throw new Exception404("응답 body가 비어있습니다.");

        return response.getBody().getValues();
    }

    public List<Place> findSimilarEmbeddings(Long placeId, Long limit) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new Exception404("해당하는 장소를 찾을 수 없습니다."));

        PlaceEmbedding placeEmbedding = placeEmbeddingRepository.findByPlaceId(place.getId())
                .orElseThrow(() -> new Exception404("해당하는 장소 임베딩을 찾을 수 없습니다."));

        String naturalLanguage = PlaceEmbedding.getNaturalLanguage(placeEmbedding, place);
        List<Long> placeIds = placeEmbeddingRepository.findTopSimilarPlaceEmbeddings(getVector(naturalLanguage), limit)
                .stream()
                .map((foundPlaceEmbedding) -> foundPlaceEmbedding.getPlaceId())
                .toList();

        return placeRepository.findAllById(placeIds);
    }

    public void deleteAllInBatch() {
        placeEmbeddingRepository.deleteAllInBatch();
    }
}
