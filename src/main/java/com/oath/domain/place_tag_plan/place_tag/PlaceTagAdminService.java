package com.oath.domain.place_tag_plan.place_tag;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceTagAdminService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final PlaceTagRepository placeTagRepository;

    // 장소와 태그 연결 로직
    @Transactional
    public PlaceTag connectPlaceAndTag(PlaceTagRequestDto.ConnectDto requestDto) {
        // 이미 연결되어 있는지 확인하여 중복 방지
        placeTagRepository.findByPlace_IdAndTag_Id(requestDto.getPlaceId(), requestDto.getTagId())
                .ifPresent(pt -> {
                    throw new IllegalStateException("The connection already exists.");
                });

        // 장소와 태그 엔티티 조회
        Place place = placeRepository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new EntityNotFoundException("Place not found with id: " + requestDto.getPlaceId()));

        Tag tag = tagRepository.findById(requestDto.getTagId())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + requestDto.getTagId()));

        // PlaceTag 관계 생성 및 저장
        PlaceTag placeTag = PlaceTag.builder()
                .place(place)
                .tag(tag)
                .createdAt(LocalDateTime.now())
                .build();

        return placeTagRepository.save(placeTag);
    }

    // 장소와 태그 연결 해제 로직
    @Transactional
    public void disconnectPlaceAndTag(PlaceTagRequestDto.ConnectDto requestDto) {
        // 연결된 관계 조회
        PlaceTag placeTag = placeTagRepository.findByPlace_IdAndTag_Id(requestDto.getPlaceId(), requestDto.getTagId())
                .orElseThrow(() -> new EntityNotFoundException("The connection does not exist."));

        // 관계 삭제
        placeTagRepository.delete(placeTag);
    }

    // 모든 장소-태그 관계 조회 로직
    public List<PlaceTag> findAllPlaceTags() {
        return placeTagRepository.findAll();
    }
}
