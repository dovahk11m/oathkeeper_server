package com.oath.domain.tracking.web;

import com.oath.domain.tracking.dto.TrackingDto;
import com.oath.domain.tracking.service.LocationService;
import com.oath.common.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService service;
    private final JwtTokenProvider jwt;

    @PostMapping("/update")
    public ResponseEntity<?> upload(
            @RequestBody TrackingDto dto,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        Optional<Long> memberIdFromToken = Optional.empty();
        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                Long mid = jwt.getMemberId(token);
                if (mid != null) memberIdFromToken = Optional.of(mid);
            }
        } catch (Exception ignored) {}

        service.upload(dto, memberIdFromToken);
        return ResponseEntity.ok(ok(null));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> recent(
            @RequestParam long planId,
            @RequestParam String bbox
    ) {
        String[] arr = bbox.split(",");
        if (arr.length != 4) {
            return ResponseEntity.badRequest().body(fail("bbox 형식: minLng,minLat,maxLng,maxLat"));
        }

        double minLng = Double.parseDouble(arr[0]);
        double minLat = Double.parseDouble(arr[1]);
        double maxLng = Double.parseDouble(arr[2]);
        double maxLat = Double.parseDouble(arr[3]);

        var data = service.listRecent(planId, minLng, minLat, maxLng, maxLat);
        return ResponseEntity.ok(ok(data));
    }

    // ---- helpers -----------------------------------------------------------
    private Map<String, Object> ok(Object data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("data", data);
        map.put("message", "OK");
        return map;
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", false);
        map.put("data", null);
        map.put("message", msg);
        return map;
    }
}
