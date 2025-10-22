package com.oath.domain.naver.map;

import com.oath.common.CommonResponse;
import com.oath.common.exception.Exception401;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naver/map")
public class NaverMapController {

    private final NaverMapService naverMapService;

    @GetMapping("/geocoding")
    public ResponseEntity<?> getPos(@RequestParam(value = "address", required = true) String address,
                                    @RequestHeader(value = "Client-ID", required = false) String clientId) {

        if (clientId == null || clientId.trim().isEmpty())
            throw new Exception401("클라이언트 ID가 올바르지 않습니다.");

        Point point = naverMapService.getPos(address, clientId);
        return ResponseEntity.ok().body(CommonResponse.success(point));
    }

    @GetMapping("/reverse-geocoding")
    public ResponseEntity<?> getAddress(@RequestParam(value = "latitude", required = true) Double latitude,
                                        @RequestParam(value = "longitude", required = true) Double longitude,
                                        @RequestHeader(value = "Client-ID", required = false) String clientId) {


        if (clientId == null || clientId.trim().isEmpty())
            throw new Exception401("클라이언트 ID가 올바르지 않습니다.");

        String address = naverMapService.getAddress(latitude, longitude, clientId);
        return ResponseEntity.ok().body(CommonResponse.success(address));
    }
}
