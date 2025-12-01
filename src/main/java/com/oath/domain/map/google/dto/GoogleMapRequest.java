package com.oath.domain.map.google.dto;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception500;
import com.oath.common.util.DateUtil;
import com.oath.domain.map.google.*;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.plan.domain.Plan;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Schema(name = "Matrix API Request", description = "구글 Matrix API의 Request를 정의하는 DTO 입니다.")
public record GoogleMapRequest(
        @Schema(description = "사용자의 위치를 받기 위한 Json 객체이며, List<Waypoint> 으로 받을 수 있습니다.", nullable = false)
        List<WayPoint> origins,

        @Schema(description = "도착지의 위치를 받기 위한 Json 객체이며, List<Waypoint> 으로 받을 수 있습니다.", nullable = false)
        List<WayPoint> destinations,

        @Schema(description = "경로 계산에서 이동 수단을 결정하는 필드이며, enum 으로 받을 수 있습니다.", nullable = true, defaultValue = "TRANSIT")
        RouteTravelMode travelMode,

        @Schema(description = "경로 계산에서 교통상황을 고려할지 결정하는 필드이며, enum 으로 받을 수 있습니다.", nullable = true, defaultValue = "TRAFFIC_AWARE")
        RoutingPreference routingPreference,

        @Schema(description = "각 출발지 위치에서 도착지까지의 거리, 걸리는 시간을 구하기 위한 필드이며, String 으로 받을 수 있습니다.", nullable = false, defaultValue = "2099-99-99 24:00", example = "yyyy-MM-dd HH:mm")
        String arrivalTime,

        @Schema(description = "거리를 어떤 단위로 보여줄지 정하는 타입이며, enum 으로 받을 수 있습니다.", nullable = true, defaultValue = "METRIC")
        Units units,

        @Schema(description = "대중교통 이용시 설정할 Json 객체입니다.", nullable = true)
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
        System.out.println(arrivalTime);

        try {
            OffsetDateTime.parse(arrivalTime);
        } catch (DateTimeParseException e) {
            arrivalTime = DateUtil.matrixTimeFormatter(arrivalTime);
        } catch (DateTimeException e) {
            throw new Exception500("유효하지 않은 시간 형식입니다: " + arrivalTime);
        }

        // 4. units가 null이면 기본값 세팅
        if (units == null) units = Units.METRIC;

        // 5. List 필드 NPE 방지
        if (origins == null || origins.isEmpty()) {
            throw new Exception400("origins 필드는 1개 이상의 출발지를 포함해야 합니다.");
        }
        if (destinations == null || destinations.isEmpty()) {
            throw new Exception400("destinations 필드는 1개 이상의 도착지를 포함해야 합니다.");
        }

        // 6. TRANSIT 모드일 때만 TransitPreferences 기본값 생성
        if (travelMode == RouteTravelMode.TRANSIT && transitPreferences == null) {
            transitPreferences = new TransitPreferences(null, null);
        }
    }

    public static GoogleMapRequest of(Plan plan, List<Place> recommendedPlaces) {
        List<GoogleMapRequest.WayPoint> origins = plan.getParticipants().stream().map(participant -> {
            return new GoogleMapRequest.WayPoint(
                    new GoogleMapRequest.WayPoint.WaypointPayload(
                            new GoogleMapRequest.WayPoint.WaypointPayload.Location(
                                    new GoogleMapRequest.WayPoint.WaypointPayload.Location.LatLng(participant.getStartLatitude().toString(), participant.getStartLongitude().toString())
                            ))
            );
        }).toList();

        List<GoogleMapRequest.WayPoint> destinations = recommendedPlaces.stream().map(place -> {
            return new GoogleMapRequest.WayPoint(
                    new GoogleMapRequest.WayPoint.WaypointPayload(
                            new GoogleMapRequest.WayPoint.WaypointPayload.Location(
                                    new GoogleMapRequest.WayPoint.WaypointPayload.Location.LatLng(place.getLat().toString(), place.getLng().toString())
                            )
                    )
            );
        }).toList();

        return new GoogleMapRequest(origins,
                destinations,
                null,
                null,
                plan.getPlanDatetime().toString(),
                null,
                null);
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
                        String latitude,
                        String longitude
                ) {
                    public LatLng {
                        Double convertedLatitude = null;
                        Double convertedLongitude = null;

                        // 1. 위도 경도가 비어있는 경우
                        if (latitude == null || latitude.trim().isEmpty() || longitude == null || longitude.trim().isEmpty())
                            throw new Exception400("좌표는 반드시 입력해주세요.");

                        // 2. 위도 경도 값이 잘못 된 경우(캐스팅 오류)
                        try {
                            convertedLatitude = Double.parseDouble(latitude);
                            convertedLongitude = Double.parseDouble(longitude);
                        } catch (NumberFormatException e) {
                            throw new Exception400("좌표는 Double 값으로 입력해주세요.");
                        }

                        // 3. 위도 경도 값이 실제와 맞지 않는 경우
                        if (convertedLatitude < -90.0 || convertedLatitude > 90.0) {
                            throw new Exception400("유효하지 않은 위도(latitude) 값입니다. (-90.0 ~ 90.0)");
                        }
                        if (convertedLongitude < -180.0 || convertedLongitude > 180.0) {
                            throw new Exception400("유효하지 않은 경도(longitude) 값입니다. (-180.0 ~ 180.0)");
                        }
                    }
                }
            }
        }
    }

    public record TransitPreferences(
            @Schema(description = "경로 계산 시 허용할 대중교통을 받는 필드이며, List<enum> 으로 받을 수 있습니다.", defaultValue = "[\"SUBWAY\", \"BUS\"]", nullable = true)
            List<TransitTravelMode> allowedTravelModes,

            @Schema(description = "경로 계산 시 어떤 부담 요소를 제한할지 선택하는 필드이며, enum 으로 받을 수 있습니다.", defaultValue = "LESS_WALKING", nullable = true)
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
