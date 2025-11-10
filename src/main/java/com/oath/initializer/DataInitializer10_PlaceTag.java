package com.oath.initializer;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(10)
public class DataInitializer10_PlaceTag implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final PlaceTagRepository placeTagRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 PlaceTag 데이터 생성 시작");

        List<Place> allPlaces = placeRepository.findAll();
        List<Tag> allTags = tagRepository.findAll();
        List<PlaceTag> placeTagsToSave = new ArrayList<>();

        if (allPlaces.isEmpty() || allTags.isEmpty()) {
            log.warn("Place 또는 Tag 데이터가 없어 PlaceTag를 생성할 수 없습니다.");
            return;
        }

        Random random = new Random();
        int minTagsPerPlace = 2; // 장소당 최소 태그 수
        int maxTagsPerPlace = 5; // 장소당 최대 태그 수

        for (Place place : allPlaces) {
            // 각 장소에 할당할 태그 수를 랜덤으로 결정
            int numberOfTagsToAssign = random.nextInt(maxTagsPerPlace - minTagsPerPlace + 1) + minTagsPerPlace;

            // 모든 태그를 섞어서 랜덤으로 선택
            Collections.shuffle(allTags, random);

            for (int i = 0; i < numberOfTagsToAssign && i < allTags.size(); i++) {
                Tag tag = allTags.get(i);
                placeTagsToSave.add(PlaceTag.builder()
                        .place(place)
                        .tag(tag)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        placeTagRepository.saveAll(placeTagsToSave);
        log.info("👷‍♂️ 샘플 PlaceTag 데이터 {}개 생성 완료", placeTagsToSave.size());
    }
}
