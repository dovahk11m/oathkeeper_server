package com.oath.domain.place_tag_plan.place;

import com.oath.common.exception.Exception404;
import com.oath.domain.plan.domain.Plan;

import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {

    private final TagRepository tagRepository;
    private final PlaceTagRepository placeTagRepository;
    private final PlaceRepository placeRepository;
    private final PlanJpaRepository planJpaRepository;

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

    public List<Place> findRecommendedPlaces(Long planId, List<String> tagNames) {
        // 1. planId로 Plan을 찾아 참여 인원 수(m)를 파악합니다.
        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("해당 계획을 찾을 수 없습니다: " + planId));
        int memberCount = plan.getParticipants().size();
        if (memberCount == 0) {
            memberCount = 1; // 참여자가 없는 경우, 최소 1명으로 계산
        }

        // 2. 반환할 장소의 최대 개수(p)를 계산합니다.
        int maxPlaceCount = 100 / memberCount;
        if (maxPlaceCount == 0) {
            return List.of(); // 100명 이상이면 추천 장소 없음
        }

        // 3. Repository에서 정렬된 장소 목록을 Pageable을 이용해 제한된 개수만큼 가져옵니다.
        return placeRepository.findPlacesByTagsOrderedByMatchCount(
                tagNames,
                PageRequest.of(0, maxPlaceCount)
        );
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
