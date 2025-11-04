package com.oath.domain.map.google;

import com.oath.common.CommonResponse;
import com.oath.common.exception.Exception401;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/google")
public class GoogleMapController {

    private final GoogleMapService googleMapService;

    @GetMapping("/matrix")
    public ResponseEntity<?> getPos(@RequestParam(value = "latitude", required = true) Double latitude,
                                    @RequestParam(value = "longitude", required = true) Double longitude,
                                    @RequestHeader(value = "Client-ID", required = false) String clientId) {

        if (clientId == null || clientId.trim().isEmpty())
            throw new Exception401("클라이언트 ID가 올바르지 않습니다.");

        return ResponseEntity.ok().body(CommonResponse.success("임시"));
    }
}
