package com.oath.initializer;

import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer6_Tag {

    private final TagRepository tagRepository;

    @Transactional
    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 태그 데이터 생성 시작");

        List<String> tags = Arrays.asList(
                "맛집", "카페", "술집", "놀거리", "사진", "공부", "산책", "데이트", "가족", "친구",
                "혼자", "단체", "가성비", "분위기", "조용한", "시끄러운", "새로운", "유명한", "숨은", "핫플",
                "한식", "일식", "중식", "양식", "분식", "디저트", "커피", "맥주", "소주", "와인",
                "전시", "공연", "영화", "쇼핑", "운동", "게임", "PC방", "노래방", "보드게임", "방탈출",
                "공원", "바다", "산", "강", "도서관", "스터디카페", "만화카페", "찜질방", "사우나", "PC방",
                "당구장", "볼링장", "골프", "테니스", "축구", "농구", "야구", "배드민턴", "등산", "자전거",
                "캠핑", "낚시", "드라이브", "여행", "호캉스", "게스트하우스", "호텔", "모텔", "펜션", "리조트",
                "맛집", "카페", "술집", "놀거리", "사진", "공부", "산책", "데이트", "가족", "친구",
                "강남", "홍대", "이태원", "종로", "명동", "신촌", "건대", "성수", "가로수길", "압구정",
                "서면", "해운대", "광안리", "남포동", "동래", "부산대", "경성대", "사상", "덕천", "연산"
        );

        tags.forEach(tagName -> {
            Tag tag = Tag.builder()
                    .name(tagName)
                    .createdAt(LocalDateTime.now())
                    .build();
            tagRepository.save(tag);
        });

        log.info("👷‍♂️ 샘플 태그 데이터 생성 완료");
    }
}
