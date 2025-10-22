package com.oath.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // DateTimeFormatter 객체를 상수로 미리 생성하여 재사용 (성능 향상)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd");

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
}
