package com.oath.common.paging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
// 페이지네이션 UI를 구성하기 위한 데이터 생성 클래스
public final class PageNumberDTO {
    // 실제페이지, 표시페이지, 현재페이지여부
    private final int number;
    private final int display;
    private final boolean isCurrent;
    public PageNumberDTO(int number, int currentPage) {
        this.number = number;
        this.display = number + 1;
        this.isCurrent = (number == currentPage);
    }
    // 페이지네이션 바 정보 생성
    public static PageNavigation createNavigation(Page<?> page, int displayRange) {
        int totalPages = page.getTotalPages();
        // 페이지가 0개인 경우 빈 네비게이션 반환
        if (totalPages == 0) {
            return new PageNavigation(new ArrayList<>(), false, false, 0, 0);
        }
        int currentPage = page.getNumber();
        // 중앙 정렬
        int startPage = Math.max(0, currentPage - displayRange / 2);
        // 범위 조정
        int endPage = Math.min(startPage + displayRange - 1, totalPages - 1);
        if (endPage - startPage < displayRange - 1) {
            startPage = Math.max(0, endPage - displayRange + 1);
        }
        // 화면에 표시될 페이지 번호 목록
        List<PageNumberDTO> pageNumbers = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(new PageNumberDTO(i, currentPage));
        }
        // '이전', '다음' 버튼 상태 계산
        boolean hasPrev = currentPage > 0;
        boolean hasNext = currentPage < totalPages - 1;
        int prevPage = hasPrev ? currentPage - 1 : 0;
        int nextPage = hasNext ? currentPage + 1 : totalPages - 1;
        // 최종 네비게이션 반환
        return new PageNavigation(pageNumbers, hasPrev, hasNext, prevPage, nextPage);
    }
    @Getter
    @AllArgsConstructor
    // 페이지네이션 바 UI를 그리는 내부 클래스
    public static class PageNavigation {
        private final List<PageNumberDTO> pageNumbers;
        private final boolean hasPrev;
        private final boolean hasNext;
        private final int prevPage;
        private final int nextPage;
    }
}

/*
### `PageNumberDTO` 사용 명세

**1. 목적**
- Spring Data의 `Page` 객체를 기반으로, 프론트엔드에서 페이지네이션 UI(페이지 번호 목록, 이전/다음 버튼 등)를 쉽게 구현할 수 있도록 필요한 모든 데이터를 계산하고 제공합니다.

**2. 주요 기능**
- `createNavigation(Page<?> page, int displayRange)`: 핵심 기능으로, 이 정적 메서드를 호출하여 `PageNavigation` 객체를 생성합니다.

**3. 사용 방법 (Service Layer)**
- `Page` 객체와 화면에 표시할 페이지 번호의 개수(`displayRange`)를 인자로 전달하여 호출합니다.
- `PageNumberDTO.createNavigation(myPageObject, 5);`

**4. 반환 객체 (`PageNavigation`) 구조**
- `pageNumbers`: `List<PageNumberDTO>` 타입. 화면에 표시될 페이지 번호들의 리스트입니다.
  - 각 `PageNumberDTO`는 `display`(화면에 보일 숫자), `isCurrent`(현재 페이지 여부) 등의 정보를 가집니다.
- `hasPrev`, `hasNext`: `boolean` 타입. '이전', '다음' 버튼의 활성화 여부를 결정합니다.
- `prevPage`, `nextPage`: `int` 타입. '이전', '다음' 버튼 클릭 시 요청할 페이지 번호입니다.
*/
