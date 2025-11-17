package com.oath.initializer;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
// @Order(8) 제거
public class DataInitializer8_Place implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 장소 데이터 생성 시작");

        List<Place> places = Arrays.asList(
                createPlace("서면역", "부산 부산진구 부전동", 35.1577, 129.0591),
                createPlace("부산시민공원", "부산 부산진구 연지동", 35.168, 129.055),
                createPlace("해운대해수욕장", "부산 해운대구 우동", 35.158, 129.160),
                createPlace("광안리해수욕장", "부산 수영구 광안동", 35.153, 129.118),
                createPlace("태종대", "부산 영도구 동삼동", 35.055, 129.089),
                createPlace("송도해수욕장", "부산 서구 암남동", 35.079, 129.019),
                createPlace("다대포해수욕장", "부산 사하구 다대동", 35.048, 128.963),
                createPlace("감천문화마을", "부산 사하구 감천동", 35.098, 129.01),
                createPlace("국제시장", "부산 중구 신창동4가", 35.100, 129.028),
                createPlace("자갈치시장", "부산 중구 남포동4가", 35.097, 129.03),
                createPlace("BIFF 광장", "부산 중구 남포동5가", 35.098, 129.029),
                createPlace("용두산공원", "부산 중구 광복동2가", 35.101, 129.032),
                createPlace("흰여울문화마을", "부산 영도구 영선동4가", 35.078, 129.044),
                createPlace("국립해양박물관", "부산 영도구 동삼동", 35.08, 129.083),
                createPlace("오륙도 스카이워크", "부산 남구 용호동", 35.093, 129.124),
                createPlace("UN기념공원", "부산 남구 대연동", 35.128, 129.098),
                createPlace("황령산 전망대", "부산 부산진구 전포동", 35.155, 129.079),
                createPlace("금정산성", "부산 금정구 금성동", 35.25, 129.05),
                createPlace("범어사", "부산 금정구 청룡동", 35.284, 129.093),
                createPlace("동래읍성", "부산 동래구 복천동", 35.205, 129.085),
                createPlace("충렬사", "부산 동래구 안락동", 35.19, 129.106),
                createPlace("부산박물관", "부산 남구 대연동", 35.13, 129.1),
                createPlace("부산미술관", "부산 해운대구 우동", 35.168, 129.138),
                createPlace("영화의전당", "부산 해운대구 우동", 35.17, 129.125),
                createPlace("벡스코", "부산 해운대구 우동", 35.169, 129.135),
                createPlace("신세계 센텀시티", "부산 해운대구 우동", 35.168, 129.129),
                createPlace("롯데백화점 부산본점", "부산 부산진구 부전동", 35.156, 129.057),
                createPlace("NC백화점 서면점", "부산 부산진구 전포동", 35.157, 129.063),
                createPlace("삼정타워", "부산 부산진구 부전동", 35.158, 129.06),
                createPlace("전포 카페거리", "부산 부산진구 전포동", 35.155, 129.063),
                createPlace("해리단길", "부산 해운대구 우동", 35.16, 129.157),
                createPlace("달맞이길", "부산 해운대구 중동", 35.158, 129.177),
                createPlace("더베이 101", "부산 해운대구 우동", 35.155, 129.151),
                createPlace("송정해수욕장", "부산 해운대구 송정동", 35.178, 129.198),
                createPlace("기장 연화리 해녀촌", "부산 기장군 기장읍 연화리", 35.22, 129.22),
                createPlace("해동용궁사", "부산 기장군 기장읍 시랑리", 35.188, 129.224),
                createPlace("아홉산숲", "부산 기장군 철마면 미동리", 35.29, 129.15),
                createPlace("죽성성당", "부산 기장군 기장읍 죽성리", 35.21, 129.23),
                createPlace("임랑해수욕장", "부산 기장군 장안읍 임랑리", 35.3, 129.25),
                createPlace("일광해수욕장", "부산 기장군 일광읍 삼성이리", 35.25, 129.23),
                createPlace("을숙도생태공원", "부산 사하구 하단동", 35.09, 128.95),
                createPlace("낙동강하구에코센터", "부산 사하구 하단동", 35.1, 128.95),
                createPlace("아미산 전망대", "부산 사하구 다대동", 35.05, 128.97),
                createPlace("몰운대", "부산 사하구 다대동", 35.04, 128.96),
                createPlace("화명생태공원", "부산 북구 화명동", 35.22, 129.0),
                createPlace("삼락생태공원", "부산 사상구 삼락동", 35.15, 128.98),
                createPlace("대저생태공원", "부산 강서구 대저1동", 35.18, 128.95),
                createPlace("렛츠런파크 부산경남", "부산 강서구 범방동", 35.2, 128.88),
                createPlace("김해국제공항", "부산 강서구 대저2동", 35.18, 128.93),
                createPlace("부산역", "부산 동구 초량동", 35.115, 129.04),
                createPlace("부산항 국제여객터미널", "부산 동구 초량동", 35.11, 129.04),
                createPlace("광복동 패션거리", "부산 중구 광복동", 35.1, 129.03),
                createPlace("보수동 책방골목", "부산 중구 보수동1가", 35.103, 129.025),
                createPlace("영도대교", "부산 영도구 대교동1가", 35.095, 129.035),
                createPlace("부산항대교", "부산 영도구 청학동", 35.09, 129.06),
                createPlace("남항대교", "부산 서구 암남동", 35.08, 129.02),
                createPlace("광안대교", "부산 해운대구 우동", 35.14, 129.13),
                createPlace("누리마루 APEC 하우스", "부산 해운대구 우동", 35.15, 129.15),
                createPlace("동백섬", "부산 해운대구 우동", 35.15, 129.15),
                createPlace("장산", "부산 해운대구 좌동", 35.18, 129.17),
                createPlace("이기대 도시자연공원", "부산 남구 용호동", 35.12, 129.12),
                createPlace("신선대", "부산 남구 용당동", 35.11, 129.08),
                createPlace("송상현광장", "부산 부산진구 부전동", 35.16, 129.06),
                createPlace("F1963", "부산 수영구 망미동", 35.16, 129.1),
                createPlace("부산현대미술관", "부산 사하구 하단동", 35.1, 128.95),
                createPlace("부산시립미술관", "부산 해운대구 우동", 35.168, 129.138),
                createPlace("부산근대역사관", "부산 중구 대청동2가", 35.102, 129.03),
                createPlace("임시수도기념관", "부산 서구 부민동2가", 35.105, 129.02),
                createPlace("복천박물관", "부산 동래구 복천동", 35.206, 129.085),
                createPlace("정관박물관", "부산 기장군 정관읍 정관로", 35.33, 129.17),
                createPlace("부산어촌민속관", "부산 북구 화명동", 35.22, 129.0),
                createPlace("해양자연사박물관", "부산 동래구 온천동", 35.21, 129.08),
                createPlace("부산대학교", "부산 금정구 장전동", 35.23, 129.08),
                createPlace("부경대학교 대연캠퍼스", "부산 남구 대연동", 35.13, 129.1),
                createPlace("한국해양대학교", "부산 영도구 동삼동", 35.08, 129.08),
                createPlace("동아대학교 부민캠퍼스", "부산 서구 부민동2가", 35.105, 129.02),
                createPlace("경성대학교", "부산 남구 대연동", 35.13, 129.1),
                createPlace("부산외국어대학교", "부산 금정구 남산동", 35.25, 129.09),
                createPlace("동의대학교 가야캠퍼스", "부산 부산진구 가야동", 35.14, 129.03),
                createPlace("신라대학교", "부산 사상구 괘법동", 35.15, 128.99),
                createPlace("고신대학교 영도캠퍼스", "부산 영도구 동삼동", 35.07, 129.07),
                createPlace("인제대학교 부산백병원", "부산 부산진구 개금동", 35.14, 129.02),
                createPlace("부산대학교병원", "부산 서구 아미동1가", 35.1, 129.02),
                createPlace("동아대학교병원", "부산 서구 대신동3가", 35.1, 129.02),
                createPlace("고신대학교복음병원", "부산 서구 암남동", 35.07, 129.02),
                createPlace("해운대백병원", "부산 해운대구 좌동", 35.17, 129.18),
                createPlace("사직야구장", "부산 동래구 사직동", 35.19, 129.06),
                createPlace("부산아시아드주경기장", "부산 연제구 거제동", 35.19, 129.06),
                createPlace("스포원파크", "부산 금정구 두구동", 35.27, 129.1),
                createPlace("강서체육공원", "부산 강서구 대저2동", 35.2, 128.95),
                createPlace("구덕운동장", "부산 서구 서대신동3가", 35.11, 129.01),
                createPlace("기장-현대차 드림 볼파크", "부산 기장군 일광읍", 35.25, 129.2),
                createPlace("부산시청", "부산 연제구 연산동", 35.18, 129.07),
                createPlace("금정구청", "부산 금정구 부곡동", 35.24, 129.09),
                createPlace("해운대구청", "부산 해운대구 중동", 35.16, 129.16),
                createPlace("부산진구청", "부산 부산진구 부암동", 35.16, 129.05),
                createPlace("동래구청", "부산 동래구 복천동", 35.2, 129.08),
                createPlace("사하구청", "부산 사하구 당리동", 35.1, 128.99),
                createPlace("북구청", "부산 북구 구포동", 35.2, 129.0),
                createPlace("남구청", "부산 남구 대연동", 35.13, 129.1),
                createPlace("수영구청", "부산 수영구 남천동", 35.14, 129.11),
                createPlace("연제구청", "부산 연제구 연산동", 35.17, 129.08),
                createPlace("영도구청", "부산 영도구 청학동", 35.09, 129.05),
                createPlace("중구청", "부산 중구 대청동4가", 35.1, 129.03),
                createPlace("서구청", "부산 서구 토성동4가", 35.1, 129.02),
                createPlace("동구청", "부산 동구 수정동", 35.12, 129.04),
                createPlace("강서구청", "부산 강서구 대저1동", 35.21, 128.98),
                createPlace("기장군청", "부산 기장군 기장읍", 35.24, 129.21)
        );

        placeRepository.saveAll(places);

        log.info("👷‍♂️ 샘플 장소 데이터 생성 완료");
    }

    private Place createPlace(String name, String address, double lat, double lng) {
        return Place.builder()
                .name(name)
                .address(address)
                .lat(lat)
                .lng(lng)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
