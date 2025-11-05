package com.oath.common.util;

import com.oath.common.exception.Exception400;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtil {

    // DateTimeFormatter 객체를 상수로 미리 생성하여 재사용 (성능 향상)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final DateTimeFormatter DATE_KST_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
    private static final DateTimeFormatter DATETIME_MATRIX_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // TimeZone 정의
    private static final ZoneId USER_TIMEZONE = ZoneId.of("Asia/Seoul");

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

    // 날짜를 yyyy월 MM월 dd일 HH시 mm분 형식으로 반환
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

    // Google Matrix API를 위한 Time Formatter
    // 이때, time은 무조건 yyyy-MM-dd HH:mm 이라고 가정
    public static String matrixTimeFormatter(String time) {
        if (time == null)
            throw new Exception400("시간은 필수입니다.");

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(time, DATETIME_MATRIX_FORMATTER);
            ZonedDateTime zonedDateTimeKST = localDateTime.atZone(USER_TIMEZONE);
            Instant instantUTC = zonedDateTimeKST.toInstant();
            return instantUTC.toString();
        } catch (DateTimeParseException e) {
            throw new Exception400("yyyy-MM-dd HH:mm 형식을 지켜주세요");
        }
    }
}
