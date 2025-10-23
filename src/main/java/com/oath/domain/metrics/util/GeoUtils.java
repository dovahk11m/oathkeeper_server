// com/oath/domain/metrics/util/GeoUtils.java
package com.oath.domain.metrics.util;

public final class GeoUtils {
    private GeoUtils() {}

    /** 위경도(도 단위) 거리(km) */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2){
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }

    /** 소수점 2자리 반올림 */
    public static double round2(double v){ return Math.round(v * 100.0) / 100.0; }
}
