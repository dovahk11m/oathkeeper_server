package com.oath.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtil {

    // DateTimeFormatter 객체를 상수로 미리 생성하여 재사용 (성능 향상)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final DateTimeFormatter DATE_KST_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초");

    // private 생성자로 외부에서의 인스턴스화를 방지
    private DateUtil() {
    }

    // 날짜와 시간을 "yy-MM-dd HH:mm" 형식으로 변환
    public static String format(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DATETIME_FORMATTER);
    }

    // 날짜를 "yy/MM/dd" 형식으로 변환
    public static String formatDate(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DATE_FORMATTER);
    }

    // 날짜를 yyyy월 MM월 dd일 HH시 mm분 ss초 형식으로 반환
    public static String formatKSTDate(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DATE_KST_FORMATTER);
    }

    // 날짜를 요일로 반환 (EN)
    public static String formatWeekendWithEnglish(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    // 날짜를 요일로 반환 (KR)
    public static String formatWeekendWithKorean(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
    }
}
