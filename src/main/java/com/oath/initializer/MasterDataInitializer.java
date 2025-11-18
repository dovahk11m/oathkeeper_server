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
@Order(2)
@RequiredArgsConstructor
public class MasterDataInitializer implements CommandLineRunner {

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

        dataInitializer2_Member.initialize(); // initialize() 호출
        dataInitializer4_Group.initialize();
        dataInitializer5_Chat.initialize();
        dataInitializer6_Tag.initialize();
        dataInitializer8_Place.initialize();
        dataInitializer10_PlaceTag.initialize();
        dataInitializer12_Plan.initialize();
        dataInitializer22_Visitor.initialize();
        dataInitializer44_Term.initialize();

        log.info("========= MasterDataInitializer: 데이터 초기화 완료 =========");
    }
}
