package com.oath.domain.place_tag_plan.place;

import com.oath.common.exception.Exception404;
import com.oath.domain.map.google.GoogleMapService;
import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import com.oath.domain.place_tag_plan.place.dto.PlaceResponse;
import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {

    private final TagRepository tagRepository;
    private final PlaceTagRepository placeTagRepository;
    private final PlaceRepository placeRepository;
    private final PlanJpaRepository planJpaRepository;
    private final GoogleMapService googleMapService;

    public List<Long> findPlaceIdsByTagName(String tagName) {
        // 1. 태그 이름으로 Tag 엔티티를 찾습니다.
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new Exception404("해당 태그를 찾을 수 없습니다: " + tagName));

        // 2. 찾은 Tag 엔티티와 연결된 모든 PlaceTag 관계를 조회합니다.
        List<PlaceTag> placeTags = placeTagRepository.findByTag(tag);

        // 3. PlaceTag 목록에서 Place ID만 추출하여 리스트로 반환합니다.
        return placeTags.stream()
                .map(placeTag -> placeTag.getPlace().getId())
                .collect(Collectors.toList());
    }

    @Transactional // DTO 변환 시 placeTags를 지연 로딩해야 하므로 readOnly = false로 변경
    public PlaceResponse.RecommendListPlace findRecommendedPlaces(Long planId, List<String> tagNames) {
        log.info("장소 추천 시작: planId={}, tagNames={}", planId, tagNames);

        // 1. planId로 Plan을 찾아 참여 인원 수(m)를 파악합니다.
        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("해당 계획을 찾을 수 없습니다: " + planId));
        log.info("Plan 찾음: planId={}, 참여자 수={}", plan.getId(), plan.getParticipants().size());

        int memberCount = plan.getParticipants().size();
        if (memberCount == 0) {
            log.warn("참여자가 0명이므로, 최소 1명으로 계산합니다.");
            memberCount = 1; // 참여자가 없는 경우, 최소 1명으로 계산
        }

        // 2. 반환할 장소의 최대 개수(p)를 계산합니다.
        int maxPlaceCount = 100 / memberCount;
        log.info("계산된 최대 장소 개수: {}", maxPlaceCount);

        if (maxPlaceCount == 0) {
            log.warn("최대 장소 개수가 0이므로, 빈 리스트를 반환합니다.");
//            return List.of(); // 100명 이상이면 추천 장소 없음
            return null; // 100명 이상이면 추천 장소 없음
        }

        // 3. Repository에서 정렬된 장소 목록을 Pageable을 이용해 제한된 개수만큼 가져옵니다.
        try {
            List<Place> recommendedPlaces = placeRepository.findPlacesByTagsOrderedByMatchCount(
                    tagNames,
                    PageRequest.of(0, maxPlaceCount)
            );
            log.info("DB 조회 완료. 추천된 장소 개수: {}", recommendedPlaces.size());

            // 4. 조회된 place 엔티티 목록과 참여자 목록으로 Google Matrix API 호출
            GoogleMapResponse googleMapResponse = matrixHelper(plan, recommendedPlaces);

            // 5. 응답받은 Matrix API로 각각 기준에 맞게 계산해서 장소 두개만 반환

            // 일단 response 에서 Elements 저장
            List<GoogleMapResponse.GoogleMapRouteMatrixElement> elements = googleMapResponse.matrixElements();

            // 1. 최소 이동 거리 합 지점 구하기
            Place matchPlace = null;
            Long minDistance = 0L;
            Long distance = 0L;
            for (int i = 0; i < recommendedPlaces.size(); i++) {
                for (int j = 0; j < elements.size(); j++) {
                    distance += elements.get(j).distanceMeters();

                    if (j == (elements.size() / recommendedPlaces.size()) - 1) {
                        if (minDistance == 0 || (i > 0 && distance < minDistance)) {
                            minDistance = distance;
                            matchPlace = recommendedPlaces.get(i);
                        }

                        distance = 0L;
                    }
                }
            }

            List<PlaceResponse.RecommendDetailPlace> centerOfMinimumAggregatePlace =
                    elements.stream().map((element) -> {


                        if () {


                            return PlaceResponse.RecommendDetailPlace.builder()
                                    .participant(plan.getParticipants().get(element.originIndex().intValue()))
                                    .destination(matchPlace)
                                    .distance(element.distanceMeters())
                                    .duration(element.duration())
                                    .build();
                        }
                    }).toList();

            return PlaceResponse.RecommendListPlace.builder()
                    .recommendedPlaces(centerOfMinimumAggregatePlace)
                    .build();

            // 4. 조회된 Place 엔티티 목록을 PlaceResponseDto 목록으로 변환합니다.
            // 이 과정에서 place.getPlaceTags()가 호출되며, 지연 로딩된 데이터가 조회됩니다.
//            return recommendedPlaces.stream()
//                    .map(PlaceResponseDto::new)
//                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("장소 추천 DB 조회 중 예외 발생", e);
            throw e; // 예외를 다시 던져서 MyExceptionHandler가 처리하도록 함
        }
    }

    private GoogleMapResponse matrixHelper(Plan plan, List<Place> recommendedPlaces) {

        GoogleMapResponse response = googleMapService.getMatrix(GoogleMapRequest.of(plan, recommendedPlaces));
        System.out.println(response);
        return response;
    }

    public Long findPlaceIdByName(String placeName) {
        Place place = placeRepository.findByName(placeName)
                .orElseThrow(() -> new Exception404("해당 장소를 찾을 수 없습니다: " + placeName));
        return place.getId();
    }

    public List<String> autocompletePlaceNames(String prefix) {
        Pageable limit = PageRequest.of(0, 10); // 최대 10개까지 결과 제한
        List<Place> places = placeRepository.findByNameStartingWith(prefix, limit);
        return places.stream()
                .map(Place::getName)
                .collect(Collectors.toList());
    }
}
