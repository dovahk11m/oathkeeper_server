package com.oath.initializer;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
// @Order(10) 제거
public class DataInitializer10_PlaceTag {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final PlaceTagRepository placeTagRepository;


    @Transactional
    public void initialize(String... args) throws Exception {

        log.info("👷‍♂️ 샘플 PlaceTag 데이터 생성 시작");

        List<Place> places = placeRepository.findAll();
        List<Tag> tags = tagRepository.findAll();
        Random random = new Random();
        int count = 0;

        if (!tags.isEmpty()) {
            for (Place place : places) {
                int numberOfTags = 1 + random.nextInt(3); // 1~3개의 태그를 랜덤으로 할당
                for (int i = 0; i < numberOfTags; i++) {
                    Tag randomTag = tags.get(random.nextInt(tags.size()));
                    PlaceTag placeTag = PlaceTag.builder()
                            .place(place)
                            .tag(randomTag)
                            .build();
                    placeTagRepository.save(placeTag);
                    count++;
                }
            }
        }

        log.info("👷‍♂️ 샘플 PlaceTag 데이터 {}개 생성 완료", count);
    }
}
