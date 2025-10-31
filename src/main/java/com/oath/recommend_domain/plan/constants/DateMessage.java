package com.oath.recommend_domain.plan.constants;

import java.lang.reflect.Field;

public class DateMessage {
    /**
     * 주요 기념일을 상수화(우리나라 + 세계 통용)
     */
    private final static String NEW_YEAR_1_1_ha = "새해";
    private final static String INDEPENDENCE_MOVEMENT_DAY_3_1_ha = "삼일절";
    private final static String APRIL_FOOLS_DAY_4_1_an = "만우절";
    private final static String CHILDREN_DAY_5_5_ha = "어린이날";
    private final static String PARENTS_DAY_5_8_an = "어버이날";
    private final static String TEACHERS_DAY_5_15_an = "스승의날";
    private final static String MEMORIAL_DAY_6_6_ha = "현충일";
    private final static String LIBERATION_DAY_8_15_ha = "광복절";
    private final static String NATIONAL_FOUNDATION_DAY_10_3_ha = "개천절";
    private final static String HANGUL_DAY_10_9_ha = "한글날";
    private final static String HALLOWEEN_10_31_an = "할로윈";
    private final static String PEPERO_DAY_11_11_an = "빼빼로데이";
    private final static String CHRISTMAS_EVE_12_24_ha = "크리스마스이브";
    private final static String CHRISTMAS_12_25_ha = "크리스마스";
    private final static String NEW_YEARS_EVE_12_31_an = "연말";

    public static String buildDateNaturalLanguage(String weekend, String month, String day) throws IllegalAccessException {
        return matchingDate(weekend, month, day);
    }

    private static String matchingDate(String weekend, String month, String day) throws IllegalAccessException {
        String formattedWeekend = matchingWeekend(weekend, month, day);
        String formattedDefaultDate = month + "월 " + day + "일(" + formattedWeekend + ")";

        for (Field field : DateMessage.class.getDeclaredFields()) {
            if (field.getName().contains(month + "_" + day))
                return formattedDefaultDate + " " + field.get(new Object()).toString() + "에 ";
        }

        return formattedDefaultDate + "에 ";
    }

    private static String matchingWeekend(String weekend, String month, String day) {
        if (weekend.contains("토요일") || weekend.contains("일요일")) {
            return "주말";
        } else if (checkDayType(month, day)) {
            return "공휴일";
        } else {
            return "평일";
        }
    }

    private static boolean checkDayType(String month, String day) {
        for (Field field : DateMessage.class.getDeclaredFields()) {
            if (field.getName().contains(month + "_" + day + "_h")) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            System.out.println(buildDateNaturalLanguage("월요일", "12", "23"));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
