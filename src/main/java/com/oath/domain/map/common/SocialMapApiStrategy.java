package com.oath.domain.map.common;

import com.oath.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public interface SocialMapApiStrategy {
    default <T> Object toGeocoding(T requestData) {
        return ResponseEntity.ok(CommonResponse.success("OO시 OO구 OO동", "geocoding 완료됨"));
    }

    default <T> Object toReverseGeocoding(T requestData) {
        return ResponseEntity.ok(CommonResponse.success(new Point(0, 0), "reverse geocoding 완료됨"));
    }

    default <T> Object getMatrix(T requestData) {
        return ResponseEntity.ok(CommonResponse.success(null, "Matrics 응답 완료됨"));
    }

    boolean match(SocialMapType type);
}
