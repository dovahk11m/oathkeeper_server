package com.oath.domain.place_tag_plan.place;

import com.oath.common.CommonResponse;
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

    @Operation(summary = "태그로 장소 검색", description = "특정 태그 이름을 가진 모든 장소의 ID 목록을 반환합니다.")
    @GetMapping("/search-by-tag")
    public ResponseEntity<CommonResponse<List<Long>>> findPlacesByTag(
            @Parameter(description = "검색할 태그 이름", required = true) @RequestParam("tagName") String tagName) {
        List<Long> placeIds = placeService.findPlaceIdsByTagName(tagName);
        return ResponseEntity.ok(CommonResponse.success(placeIds, "태그로 장소 검색 성공"));
    }

    @Operation(summary = "태그 기반 장소 추천", description = "여러 태그와 계획(plan) ID를 기반으로 장소를 추천합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<CommonResponse<List<Place>>> recommendPlaces(
            @Parameter(description = "장소 추천의 기반이 될 계획(plan)의 ID", required = true) @RequestParam Long planId,
            @Parameter(description = "추천에 사용할 태그 이름 목록", required = true) @RequestParam List<String> tagNames) {
        List<Place> recommendedPlaces = placeService.findRecommendedPlaces(planId, tagNames);
        return ResponseEntity.ok(CommonResponse.success(recommendedPlaces, "장소 추천 성공"));
    }

    @Operation(summary = "이름으로 장소 ID 검색", description = "장소 이름으로 검색하여 해당하는 장소의 ID를 반환합니다.")
    @GetMapping("/search-by-name")
    public ResponseEntity<CommonResponse<Long>> findPlaceIdByName(
            @Parameter(description = "검색할 장소 이름", required = true) @RequestParam("placeName") String placeName) {
        Long placeId = placeService.findPlaceIdByName(placeName);
        return ResponseEntity.ok(CommonResponse.success(placeId, "이름으로 장소 검색 성공"));
    }

    @Operation(summary = "장소 이름 자동 완성", description = "입력된 접두사로 시작하는 장소 이름 목록을 반환합니다.")
    @GetMapping("/autocomplete/name")
    public ResponseEntity<CommonResponse<List<String>>> autocompletePlaceNames(
            @Parameter(description = "검색어 접두사", required = true) @RequestParam("prefix") String prefix) {
        List<String> names = placeService.autocompletePlaceNames(prefix);
        return ResponseEntity.ok(CommonResponse.success(names, "장소 이름 자동 완성 성공"));
    }

    @Operation(summary = "태그 이름 자동 완성", description = "입력된 접두사로 시작하는 태그 이름 목록을 반환합니다.")
    @GetMapping("/autocomplete/tag")
    public ResponseEntity<CommonResponse<List<String>>> autocompleteTagNames(
            @Parameter(description = "검색어 접두사", required = true) @RequestParam("prefix") String prefix) {
        List<String> names = tagService.autocompleteTagNames(prefix);
        return ResponseEntity.ok(CommonResponse.success(names, "태그 이름 자동 완성 성공"));
    }
}
