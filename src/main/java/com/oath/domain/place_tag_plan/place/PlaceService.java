package com.oath.domain.place_tag_plan.place;

import com.oath.domain.place_tag_plan.place.dto.PlaceResponse;
import com.oath.recommend_domain.place.PlaceEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceEmbeddingService placeEmbeddingService;

    public List<PlaceResponse.DetailPlace> listRecommendPlaces(Long currentPlaceId, Long limit) {
        List<Place> places = placeEmbeddingService.findSimilarEmbeddings(currentPlaceId, limit);
        return places.stream()
                .map((place) -> PlaceResponse.DetailPlace.of(place))
                .toList();
    }
}
