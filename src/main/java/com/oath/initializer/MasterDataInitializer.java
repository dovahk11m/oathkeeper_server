package com.oath.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@Order(1) // 가장 먼저 실행되도록 설정
@RequiredArgsConstructor
public class MasterDataInitializer implements CommandLineRunner {

    private final DataInitializer0_Cleanup dataInitializer0_Cleanup; // 다시 주입받도록 복원
    private final DataInitializer2_Member dataInitializer2_Member;
    private final DataInitializer4_Group dataInitializer4_Group;
    private final DataInitializer5_Chat dataInitializer5_Chat;
    private final DataInitializer6_Tag dataInitializer6_Tag;
    private final DataInitializer8_Place dataInitializer8_Place;
    private final DataInitializer10_PlaceTag dataInitializer10_PlaceTag;
    private final DataInitializer12_Plan dataInitializer12_Plan;
    private final DataInitializer22_Visitor dataInitializer22_Visitor;
    private final DataInitializer44_Term dataInitializer44_Term;


    @Override
    public void run(String... args) throws Exception {
        log.info("========= MasterDataInitializer: 데이터 초기화 시작 =========");

        dataInitializer0_Cleanup.run(args); // 호출 복원

        dataInitializer2_Member.run(args);
        dataInitializer4_Group.run(args);
        dataInitializer5_Chat.run(args);
        dataInitializer6_Tag.run(args);
        dataInitializer8_Place.run(args);
        dataInitializer10_PlaceTag.run(args);
        dataInitializer12_Plan.run(args);
        dataInitializer22_Visitor.run(args);
        dataInitializer44_Term.run(args);

        log.info("========= MasterDataInitializer: 데이터 초기화 완료 =========");
    }
}
