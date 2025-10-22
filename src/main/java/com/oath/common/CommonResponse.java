package com.oath.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 모든 API 응답을 위한 표준 래퍼(Wrapper) 클래스입니다.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CommonResponse<T> {
    private boolean success;
    private T data;
    private String message;

    //성공 (데이터, 메시지)
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, data, message);
    }
    //성공 (데이터)
    public static <T> CommonResponse<T> success(T data) {
        return success(data, null);
    }
    //실패
    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(false, null, message);
    }
}
