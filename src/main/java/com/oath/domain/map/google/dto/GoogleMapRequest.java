package com.oath.domain.map.google.dto;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception500;
import com.oath.common.util.DateUtil;
import com.oath.domain.map.google.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public record GoogleMapRequest(
        List<WayPoint> origins,
        List<WayPoint> destinations,
        RouteTravelMode travelMode,
        RoutingPreference routingPreference,
        String arrivalTime,
        Units units,
        TransitPreferences transitPreferences
) {
    public GoogleMapRequest {

        // 1. travelMode가 null이면 기본값 세팅
        if (travelMode == null)
            travelMode = RouteTravelMode.TRANSIT;

        // 2. routingPreference가 null이면 기본값 세팅
        if (travelMode == RouteTravelMode.TRANSIT) {
            // 'TRANSIT' 모드일 때는 Google이 null을 원하므로 'null'로 강제
            routingPreference = null;
        } else {
            // 'TRANSIT' 모드가 아닐 때만, 'routingPreference'가 null이면 기본값 설정
            if (routingPreference == null) {
                routingPreference = RoutingPreference.TRAFFIC_AWARE;
            }
        }

        // 3. departureTime 검증
        if (arrivalTime == null) throw new Exception400("도착 시간은 필수입니다.");

        try {
            OffsetDateTime.parse(arrivalTime);
        } catch (DateTimeParseException e) {
            arrivalTime = DateUtil.matrixTimeFormatter(arrivalTime);
        } catch (RuntimeException e) {
            throw new Exception500("도착 시간 형식 변환 중 서버 내부 오류가 발생했습니다.");
        }

        // 4. units가 null이면 기본값 세팅
        if (units == null) units = Units.METRIC;

        // 5. List 필드 NPE 방지
        if (origins == null) {
            origins = List.of();
        }
        if (destinations == null) {
            destinations = List.of();
        }

        // 6. TRANSIT 모드일 때만 TransitPreferences 기본값 생성
        if (travelMode == RouteTravelMode.TRANSIT && transitPreferences == null) {
            transitPreferences = new TransitPreferences(null, null);
        }
    }

    public record WayPoint(
            WaypointPayload waypoint
    ) {
        public record WaypointPayload(
                Location location
        ) {
            public record Location(
                    LatLng latLng
            ) {
                public record LatLng(
                        double latitude,
                        double longitude
                ) {
                    public LatLng {
                        if (latitude < -90.0 || latitude > 90.0) {
                            throw new Exception400("유효하지 않은 위도(latitude) 값입니다. (-90.0 ~ 90.0)");
                        }
                        if (longitude < -180.0 || longitude > 180.0) {
                            throw new Exception400("유효하지 않은 경도(longitude) 값입니다. (-180.0 ~ 180.0)");
                        }
                    }
                }
            }
        }
    }

    public record TransitPreferences(
            List<TransitTravelMode> allowedTravelModes,
            TransitRoutingPreference routingPreference
    ) {
        public TransitPreferences {
            // 1. allowedTravelModes가 null이거나 isEmpty라면, 기본값 세팅
            if (allowedTravelModes == null || allowedTravelModes.isEmpty())
                allowedTravelModes = List.of(TransitTravelMode.SUBWAY, TransitTravelMode.BUS);

            // 2. routingPreference가 null이라면 기본값 세팅
            if (routingPreference == null)
                routingPreference = TransitRoutingPreference.LESS_WALKING;
        }
    }
}
