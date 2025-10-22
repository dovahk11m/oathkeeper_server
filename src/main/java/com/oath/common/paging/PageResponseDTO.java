package com.oath.common.paging;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@AllArgsConstructor
@Builder
// 페이징된 API 응답을 위한 래퍼클래스
public class PageResponseDTO<T> {

    /*
     데이터 목록, 페이지번호, 페이지당데이터
     전체페이지, 전체데이터, 마지막여부
     */
    private List<T> content;
    private int page;
    private int size;
    private int totalPage;
    private long totalElements;
    private boolean last;

    private PageNumberDTO.PageNavigation navigation;

    public static <T, E> PageResponseDTO<T> from(
            Page<E> page,
            Function<E, T> converter,
            int displayRange
    ) {
        List<T> dtoList = page.getContent().stream()
                .map(converter)
                .toList();
        PageNumberDTO.PageNavigation navigation = PageNumberDTO.createNavigation(page, displayRange);
        return PageResponseDTO.<T>builder()
                .content(dtoList)
                .page(page.getNumber())
                .size(page.getSize())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .navigation(navigation)
                .build();
    }
}

/*
### `PageResponseDTO` 사용 명세

**1. 목적**
- 모든 페이징 API의 응답 형식을 통일하기 위한 공통 DTO입니다.
- 클라이언트가 페이징된 데이터를 일관된 방식으로 처리할 수 있도록 돕습니다.

**2. 주요 기능**
- `from(Page<E> page, Function<E, T> converter, int displayRange)`: 핵심 기능으로, 이 정적 팩토리 메서드를 호출하여 `PageResponseDTO` 객체를 생성합니다.
- 제네릭을 사용하여 `Page<Member>`를 `PageResponseDTO<MemberResponseDTO>`로, `Page<Review>`를 `PageResponseDTO<ReviewResponseDTO>`로 변환하는 등 다양한 타입에 재사용할 수 있습니다.

**3. 사용 방법 (Service Layer)**
- Repository로부터 받은 `Page<Entity>` 객체와, `Entity`를 `DTO`로 변환하는 람다(또는 메서드 참조), 페이지네이션 바 길이를 인자로 전달하여 호출합니다.
- `return PageResponseDTO.from(reviewPage, ReviewResponseDTO::new, 5);`

**4. 반환 객체 (`PageResponseDTO`) 구조**
- `content`: `List<T>` 타입. DTO로 변환된 실제 데이터 목록입니다.
- `page`, `size`, `totalPage`, `totalElements`, `last`: Spring `Page` 객체가 제공하는 표준 페이징 정보입니다.
- `navigation`: `PageNumberDTO.PageNavigation` 타입. 페이지네이션 UI를 그리는 데 필요한 상세 정보입니다.
*/