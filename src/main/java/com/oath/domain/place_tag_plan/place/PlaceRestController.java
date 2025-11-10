package com.oath.domain.place_tag_plan.place;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.domain.place_tag_plan.place.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceRestController {

    private final PlaceService placeService;

    // 추천 장소 목록 조회
    @Auth
    @GetMapping("recommend")
    public ResponseEntity<CommonResponse<List<PlaceResponse.DetailPlace>>> listRecommendPlaces(@RequestParam("currentPlaceId") Long currentPlaceId,
                                                                                               @RequestParam("limit") Long limit) {

        List<PlaceResponse.DetailPlace> places = placeService.listRecommendPlaces(currentPlaceId, limit);
        return ResponseEntity.ok(CommonResponse.success(places, places.size() + "개의 장소가 추천 검색되었습니다."));
    }
}
