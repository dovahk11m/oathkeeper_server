package com.oath.domain.map.google.dto;

import com.oath.common.exception.Exception400;
import com.oath.domain.map.google.*;

import java.lang.reflect.Method;
import java.util.List;

public record GoogleMapRequest(
        List<WayPoint> origins,
        List<WayPoint> destinations,
        String travelMode,
        String routingPreference,
        String departureTime,
        String units,
        TransitPreferences transitPreferences
) {
    public boolean validate() {
        try {
            boolean isValidated = false;

            // 1. TravelMode 검증
            Method travelModeMethod = RouteTravelMode.class.getDeclaredMethod("equals", String.class);
            isValidated = (boolean) travelModeMethod.invoke(boolean.class, this.travelMode);

            // 2. RoutingPreference 검증
            Method routingPreferenceMethod = RoutingPreference.class.getDeclaredMethod("equals", String.class);
            isValidated = (boolean) routingPreferenceMethod.invoke(boolean.class, this.routingPreference);

            // 3. Units 검증
            Method unitsMethod = Units.class.getDeclaredMethod("equals", String.class);
            isValidated = (boolean) unitsMethod.invoke(boolean.class, this.units);

            // 4. TransitTravelMode 검증
            Method transitTravelModeMethod = TransitTravelMode.class.getDeclaredMethod("equals", String.class);
            for (String travelMode : this.transitPreferences.allowedTransitTravelModes) {
                isValidated = (boolean) transitTravelModeMethod.invoke(boolean.class, travelMode);
            }

            // 5. RoutingPreference 검증
            Method transitRoutingPreferenceMethod = TransitRoutingPreference.class.getDeclaredMethod("equals", String.class);
            isValidated = (boolean) transitRoutingPreferenceMethod.invoke(boolean.class, transitPreferences.transitRoutingPreference);

            if (!isValidated) throw new Exception400("잘못된 요청 타입입니다");
            return isValidated;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record WayPoint(
            Location location
    ) {
        public record Location(
                LatLng latLng
        ) {
            public record LatLng(
                    double latitude,
                    double longitude
            ) {
                public LatLng(double latitude, double longitude) {
                    this.latitude = latitude;
                    this.longitude = longitude;

                    if (latitude == 0.0 || longitude == 0.0) throw new Exception400("좌표정보는 반드시 포함되어야 합니다.");
                }
            }
        }
    }

    public record TransitPreferences(
            List<String> allowedTransitTravelModes,
            String transitRoutingPreference
    ) {
    }
}
