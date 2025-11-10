package com.oath.domain.place_tag_plan;

import com.oath.common.CommonResponse;
import com.oath.domain.place_tag_plan.place.PlaceService;
import com.oath.domain.place_tag_plan.place.dto.PlaceResponse;
import com.oath.domain.place_tag_plan.tag.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Place & Tag API", description = "장소 및 태그 관련 API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final TagService tagService;

    @Operation(summary = "태그 기반 추천 장소 거리 계산", description = "여러 태그와 계획(plan) ID를 기반으로 장소를 추천합니다.")
    @GetMapping("/recommend/with-tags")
    public ResponseEntity<CommonResponse<PlaceResponse.RecommendListPlace>> recommendPlaces(
            @Parameter(description = "장소 추천의 기반이 될 계획(plan)의 ID", required = true) @RequestParam(name = "planId") Long planId,
            @Parameter(description = "추천에 사용할 태그 이름 목록", required = true) @RequestParam(name = "tagNames") List<String> tagNames) {
//        List<PlaceResponseDto> recommendedPlaces = placeService.findRecommendedPlaces(planId, tagNames);
        PlaceResponse.RecommendListPlace places = placeService.findRecommendedPlaces(planId, tagNames);
        return ResponseEntity.ok(CommonResponse.success(places, "장소 추천 성공"));
    }

    @Operation(summary = "지정 장소 거리 계산", description = "장소 이름으로 검색하여 해당하는 장소의 ID를 반환합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<CommonResponse<Long>> findPlaceIdByName(
            @Parameter(description = "검색할 장소 이름", required = true) @RequestParam("placeName") String placeName) {
        Long placeId = placeService.findPlaceIdByName(placeName);
        return ResponseEntity.ok(CommonResponse.success(placeId, "이름으로 장소 검색 성공"));
    }

    @Operation(summary = "모든 장소 리스트 가져오기", description = "입력된 접두사로 시작하는 장소 이름 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<String>>> autocompletePlaceNames(
            @Parameter(description = "검색어 접두사", required = true) @RequestParam("prefix") String prefix) {
        List<String> names = placeService.autocompletePlaceNames(prefix);
        return ResponseEntity.ok(CommonResponse.success(names, "장소 이름 자동 완성 성공"));
    }

    @Operation(summary = "모든 태그 리스트 가져오기", description = "입력된 접두사로 시작하는 태그 이름 목록을 반환합니다.")
    @GetMapping("/with-tags")
    public ResponseEntity<CommonResponse<List<String>>> autocompleteTagNames(
            @Parameter(description = "검색어 접두사", required = true) @RequestParam("prefix") String prefix) {
        List<String> names = tagService.autocompleteTagNames(prefix);
        return ResponseEntity.ok(CommonResponse.success(names, "태그 이름 자동 완성 성공"));
    }
}
