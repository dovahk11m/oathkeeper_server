package com.oath.domain.map.google;

import com.oath.common.CommonResponse;
import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/google")
public class GoogleMapController {

    private final GoogleMapService googleMapService;

    @PostMapping("/matrix")
    public ResponseEntity<?> getPos(@RequestBody GoogleMapRequest googleMapRequest) {

        GoogleMapResponse response = googleMapService.getMatrix(googleMapRequest);
        return ResponseEntity.ok().body(CommonResponse.success(response, "각 거리 계산이 완료 되었습니다."));
    }
}
