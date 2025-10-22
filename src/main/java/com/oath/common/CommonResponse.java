package com.oath.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
