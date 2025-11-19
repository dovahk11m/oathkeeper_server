package com.oath.domain.place_tag_plan.place;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceAdminService {

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    private final PlaceRepository placeRepository;

    // 장소 생성 로직
    @Transactional
    public Place createPlace(PlaceRequestDto.CreatePlaceDto requestDto) {
        Place place = Place.builder()
                .name(requestDto.getName())
                .address(requestDto.getAddress())
                .lat(requestDto.getLat())
                .lng(requestDto.getLng())
                .description(requestDto.getDescription())
                .imageUrl(requestDto.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();
        return placeRepository.save(place);
    }

    // 장소 수정 로직
    @Transactional
    public Place updatePlace(Long placeId, PlaceRequestDto.UpdatePlaceDto requestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found with id: " + placeId));

        place.setName(requestDto.getName());
        place.setAddress(requestDto.getAddress());
        place.setLat(requestDto.getLat());
        place.setLng(requestDto.getLng());
        place.setDescription(requestDto.getDescription());
        place.setImageUrl(requestDto.getImageUrl());
        place.setUpdatedAt(LocalDateTime.now());

        return placeRepository.save(place);
    }

    // 장소 삭제 로직
    @Transactional
    public void deletePlace(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new EntityNotFoundException("Place not found with id: " + placeId);
        }
        placeRepository.deleteById(placeId);
    }

    // 모든 장소 조회 로직
    public List<Place> findAllPlaces() {
        return placeRepository.findAll();
    }

    // ID로 장소 조회 로직
    public Place findPlaceById(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found with id: " + placeId));
    }

    public PlaceResponseDto.PlaceDto searchPlace(String keyword) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query="
                + UriUtils.encode(keyword, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PlaceResponseDto.PlaceDto> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, PlaceResponseDto.PlaceDto.class);

        return response.getBody();

    }

    public Place savePlace(PlaceRequestDto.PlaceDto reqDto) {
        Place place = Place.builder()
                .name(reqDto.getName())
                .address(reqDto.getAddress())
                .lat(reqDto.getLat())
                .lng(reqDto.getLng())
                .build();

        return placeRepository.save(place);
    }
}
