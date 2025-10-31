package com.oath.initializer;

import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(6)
public class DataInitializer6_Tag implements CommandLineRunner {

    private final TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 태그 데이터 생성 시작");

        List<String> tagNames = Arrays.asList(
                // 업종
                "맛집", "카페", "술집", "한식", "일식", "중식", "양식", "분식",
                // 분위기
                "분위기", "조용", "시끌", "데이트", "가족", "회식",
                // 특징
                "가성비", "고급", "자연산", "뷰", "인스타", "블로그", "블루리본", "미슐랭",
                // 편의시설
                "주차", "예약", "단체", "룸", "발렛",
                //취미
                "낚시", "게임", "영화", "애니",
                //휴식
                "명상",  "공원",  "마사지",  "자연",  "바다",  "산",
                //패션
                "쇼핑", "아울렛", "명품", "편집숍", "화장품", "뷰티",
                //동물
                "반려동물", "고양이", "개", "애견카페", "고양이카페", "애견용품",
                //스포츠
                "축구", "야구", "농구", "배구", "스포츠용품",
                //종교역사
                "사찰",  "절",  "성당",  "교회",  "보물",  "유적",  "사당",
                // 기타
                "스터디", "업무"
        );

        for (String name : tagNames) {
            Tag tag = Tag.builder()
                    .name(name)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            tagRepository.save(tag);
        }

        log.info("👷‍♂️ 샘플 태그 데이터 생성 완료");
    }
}
