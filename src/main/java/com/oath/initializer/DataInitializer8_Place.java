package com.oath.initializer;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
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
@Order(8)
public class DataInitializer8_Place implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 장소 데이터 생성 시작");

        List<Place> allPlaces = Arrays.asList(
            Place.builder().name("동매").address("1호선 동매").lat(35.0899).lng(128.9742).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("신평").address("1호선 신평").lat(35.095179).lng(128.960564).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("하단").address("1호선 하단 (부산본병원)").lat(35.10618).lng(128.966803).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("당리").address("1호선 당리 (사하구청)").lat(35.103532).lng(128.973846).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("사하").address("1호선 사하").lat(35.099847).lng(128.9831).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("괴정").address("1호선 괴정").lat(35.099816).lng(128.992144).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("대티").address("1호선 대티 (동주대학)").lat(35.103126).lng(128.999936).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("서대신").address("1호선 서대신").lat(35.110937).lng(129.012178).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동대신").address("1호선 동대신").lat(35.110452).lng(129.017684).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("토성").address("1호선 토성").lat(35.100727).lng(129.019776).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("자갈치").address("1호선 자갈치").lat(35.098495).lng(129.02667).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("남포").address("1호선 남포 (영도대교)").lat(35.097589).lng(129.034606).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("중앙").address("1호선 중앙").lat(35.100908).lng(129.040186).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("부산역").address("1호선 부산역").lat(35.113337).lng(129.03923).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("초량").address("1호선 초량").lat(35.121528).lng(129.043441).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("좌천").address("1호선 좌천").lat(35.128796).lng(129.050519).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("범일").address("1호선 범일 (자유시장·BC카드)").lat(35.132514).lng(129.060105).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("범내골").address("1호선 범내골").lat(35.143621).lng(129.063228).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("서면").address("1호선 서면").lat(35.157833).lng(129.057398).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("부전").address("1호선 부전").lat(35.163618).lng(129.060233).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("양정").address("1호선 양정").lat(35.172965).lng(129.072237).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("시청").address("1호선 시청 (연제)").lat(35.179555).lng(129.077274).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("연산").address("1호선 연산").lat(35.186259).lng(129.081896).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("교대").address("1호선 교대").lat(35.18957).lng(129.085732).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동래").address("1호선 동래").lat(35.201476).lng(129.083324).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("명륜").address("1호선 명륜").lat(35.210419).lng(129.079203).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("온천장").address("1호선 온천장 (부산가톨릭대학교)").lat(35.220197).lng(129.075904).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("부산대").address("1호선 부산대").lat(35.230718).lng(129.08865).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("장전").address("1호선 장전").lat(35.239322).lng(129.091723).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("구서").address("1호선 구서 (롯데백화점)").lat(35.245842).lng(129.096323).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("두실").address("1호선 두실").lat(35.25301).lng(129.099166).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("남산").address("1호선 남산").lat(35.256877).lng(129.102604).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("범어사").address("1호선 범어사").lat(35.263806).lng(129.102607).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("노포").address("1호선 노포 (종합버스터미널)").lat(35.27513).lng(129.10179).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("다대포해수욕장").address("1호선 다대포해수욕장").lat(35.05937).lng(128.97155).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("다대포항").address("1호선 다대포항 (낫개)").lat(35.068224).lng(128.971638).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("낫개").address("1호선 낫개").lat(35.074697).lng(128.969185).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동매").address("1호선 동매").lat(35.084803).lng(128.965935).description("부산지하철 1호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("장산").address("2호선 장산 (해운대백병원)").lat(35.169914).lng(129.176986).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("중동").address("2호선 중동").lat(35.1667).lng(129.168604).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("해운대").address("2호선 해운대").lat(35.163672).lng(129.158908).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동백").address("2호선 동백").lat(35.161484).lng(129.147897).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("벡스코").address("2호선 벡스코 (시립미술관)").lat(35.168844).lng(129.138933).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("센텀시티").address("2호선 센텀시티 (BEXCO·신세계)").lat(35.168827).lng(129.131745).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("민락").address("2호선 민락").lat(35.167228).lng(129.121909).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("수영").address("2호선 수영").lat(35.165227).lng(129.114713).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("광안").address("2호선 광안").lat(35.157916).lng(129.113168).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("금련산").address("2호선 금련산").lat(35.149771).lng(129.110961).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("남천").address("2호선 남천 (KBS·수영구청)").lat(35.142139).lng(129.107978).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("경성대·부경대").address("2호선 경성대·부경대 (동명대학교)").lat(35.137585).lng(129.100548).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("대연").address("2호선 대연 (고려병원)").lat(35.135153).lng(129.092161).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("못골").address("2호선 못골 (남구청)").lat(35.134731).lng(129.084415).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("지게골").address("2호선 지게골").lat(35.13117).lng(129.076846).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("문현").address("2호선 문현 (부산은행)").lat(35.131614).lng(129.066487).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("국제금융센터·부산은행").address("2호선 국제금융센터·부산은행").lat(35.140728).lng(129.064567).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("전포").address("2호선 전포").lat(35.152865).lng(129.059293).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("서면").address("2호선 서면").lat(35.158309).lng(129.057361).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("부암").address("2호선 부암").lat(35.163353).lng(129.053805).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("가야").address("2호선 가야").lat(35.164879).lng(129.043697).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동의대").address("2호선 동의대").lat(35.163467).lng(129.034873).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("개금").address("2호선 개금").lat(35.154133).lng(129.027063).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("냉정").address("2호선 냉정").lat(35.152846).lng(129.019672).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("주례").address("2호선 주례").lat(35.151703).lng(129.010419).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("감전").address("2호선 감전 (사상구청)").lat(35.164344).lng(128.995968).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("사상").address("2호선 사상").lat(35.166642).lng(128.988019).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("덕포").address("2호선 덕포").lat(35.176472).lng(128.985655).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("모라").address("2호선 모라").lat(35.19198).lng(128.986617).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("구남").address("2호선 구남 (구포시장)").lat(35.201777).lng(128.987823).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("구명").address("2호선 구명").lat(35.210452).lng(128.990494).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("덕천").address("2호선 덕천").lat(35.216399).lng(128.994348).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("수정").address("2호선 수정").lat(35.228514).lng(129.006932).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("화명").address("2호선 화명").lat(35.239303).lng(129.004183).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("율리").address("2호선 율리").lat(35.25055).lng(129.000673).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동원").address("2호선 동원").lat(35.257007).lng(128.998495).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("금곡").address("2호선 금곡").lat(35.263884).lng(128.998242).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("호포").address("2호선 호포").lat(35.275587).lng(128.999081).description("부산지하철 2호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("수영").address("3호선 수영").lat(35.167753).lng(129.11459).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("망미").address("3호선 망미 (병무청)").lat(35.171528).lng(129.108225).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("배산").address("3호선 배산").lat(35.173504).lng(129.095498).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("물만골").address("3호선 물만골").lat(35.176808).lng(129.085748).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("연산").address("3호선 연산").lat(35.186173).lng(129.081526).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("거제").address("3호선 거제 (법원·검찰청)").lat(35.188589).lng(129.073941).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("종합운동장").address("3호선 종합운동장").lat(35.19125).lng(129.067504).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("사직").address("3호선 사직").lat(35.198998).lng(129.064996).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("미남").address("3호선 미남").lat(35.205503).lng(129.068061).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("만덕").address("3호선 만덕").lat(35.213).lng(129.036527).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("남산정").address("3호선 남산정").lat(35.220194).lng(129.02701).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("숙등").address("3호선 숙등").lat(35.230784).lng(129.006932).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("덕천").address("3호선 덕천").lat(35.22687).lng(129.006456).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("구포").address("3호선 구포").lat(35.228514).lng(129.006932).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("강서구청").address("3호선 강서구청").lat(35.230784).lng(129.006932).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("체육공원").address("3호선 체육공원").lat(35.230784).lng(129.006932).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("대저").address("3호선 대저").lat(35.241512).lng(128.988295).description("부산지하철 3호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("미남").address("4호선 미남").lat(35.207116).lng(129.069172).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동래").address("4호선 동래").lat(35.204834).lng(129.077082).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("수안").address("4호선 수안").lat(35.201828).lng(129.083806).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("낙민").address("4호선 낙민").lat(35.200254).lng(129.090774).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("충렬사").address("4호선 충렬사 (안락)").lat(35.199859).lng(129.097636).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("명장").address("4호선 명장").lat(35.205143).lng(129.101517).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("서동").address("4호선 서동").lat(35.213333).lng(129.107683).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("금사").address("4호선 금사").lat(35.215829).lng(129.115153).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("반여농산물시장").address("4호선 반여농산물시장").lat(35.217779).lng(129.124061).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("석대").address("4호선 석대").lat(35.21811162).lng(129.137179).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("영산대").address("4호선 영산대 (아랫반송)").lat(35.230784).lng(129.160166).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동부산대학").address("4호선 동부산대학").lat(35.236676).lng(129.17188).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("고촌").address("4호선 고촌").lat(35.238478).lng(129.18379).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("안평").address("4호선 안평 (고촌주택단지)").lat(35.24479).lng(129.186088).description("부산지하철 4호선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("부전").address("동해선 부전").lat(35.164668).lng(129.06011).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("거제해맞이").address("동해선 거제해맞이").lat(35.182197).lng(129.069315).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("거제").address("동해선 거제 (법원·검찰청)").lat(35.188587).lng(129.073904).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("동래").address("동해선 동래").lat(35.205614).lng(129.078502).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("안락").address("동해선 안락").lat(35.195907).lng(129.101062).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("재송").address("동해선 재송").lat(35.189).lng(129.120919).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("센텀").address("동해선 센텀").lat(35.168921).lng(129.131659).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("벡스코").address("동해선 벡스코").lat(35.16899).lng(129.13868).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("신해운대").address("동해선 신해운대").lat(35.181832).lng(129.176696).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("송정").address("동해선 송정").lat(35.189569).lng(129.201657).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("기장").address("동해선 기장").lat(35.239303).lng(129.213279).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("오시리아").address("동해선 오시리아").lat(35.208112).lng(129.217354).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("시랑").address("동해선 시랑").lat(35.236676).lng(129.17188).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("일광").address("동해선 일광").lat(35.25301).lng(129.245844).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("좌천").address("동해선 좌천").lat(35.275587).lng(129.289045).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            Place.builder().name("월내").address("동해선 월내").lat(35.304562).lng(129.30907).description("부산지하철 동해선 역입니다.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );

        placeRepository.saveAll(allPlaces);

        log.info("👷‍♂️ 샘플 장소 데이터 생성 완료");
    }
}
