package com.oath.domain.place_tag_plan.place;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/places")
public class PlaceAdminController {

    private final PlaceAdminService placeAdminService;

    // 장소 목록 페이지 표시
    @GetMapping
    public String listPlaces(Model model) {
        List<Place> places = placeAdminService.findAllPlaces();
        model.addAttribute("places", places);
        return "admin/places/list"; // templates/admin/places/list.html (or .mustache) 뷰를 반환
    }

    // 새 장소 등록 폼 페이지 표시
    @GetMapping("/new")
    public String newPlaceForm(Model model) {
        model.addAttribute("place", new PlaceRequestDto.CreatePlaceDto());
        return "admin/places/new"; // templates/admin/places/new.html (or .mustache) 뷰를 반환
    }

    // 장소 생성 처리
    @PostMapping
    public String createPlace(PlaceRequestDto.CreatePlaceDto requestDto, RedirectAttributes redirectAttributes) {
        placeAdminService.createPlace(requestDto);
        redirectAttributes.addFlashAttribute("message", "장소가 성공적으로 생성되었습니다.");
        return "redirect:/admin/places";
    }

    // 장소 수정 폼 페이지 표시
    @GetMapping("/{placeId}/edit")
    public String editPlaceForm(@PathVariable Long placeId, Model model) {
        Place place = placeAdminService.findPlaceById(placeId);
        PlaceRequestDto.UpdatePlaceDto updateDto = new PlaceRequestDto.UpdatePlaceDto();
        updateDto.setName(place.getName());
        updateDto.setAddress(place.getAddress());
        updateDto.setLat(place.getLat());
        updateDto.setLng(place.getLng());
        updateDto.setDescription(place.getDescription());
        updateDto.setImageUrl(place.getImageUrl());
        model.addAttribute("placeId", placeId);
        model.addAttribute("place", updateDto);
        return "admin/places/edit"; // templates/admin/places/edit.html (or .mustache) 뷰를 반환
    }

    // 장소 수정 처리
    @PostMapping("/{placeId}") // PUT 대신 POST를 사용하여 폼 제출 처리 (HTML 폼은 PUT/DELETE 직접 지원 안함)
    public String updatePlace(@PathVariable Long placeId, PlaceRequestDto.UpdatePlaceDto requestDto, RedirectAttributes redirectAttributes) {
        placeAdminService.updatePlace(placeId, requestDto);
        redirectAttributes.addFlashAttribute("message", "장소가 성공적으로 수정되었습니다.");
        return "redirect:/admin/places";
    }

    // 장소 삭제 처리
    @PostMapping("/{placeId}/delete") // DELETE 대신 POST를 사용하여 폼 제출 처리
    public String deletePlace(@PathVariable Long placeId, RedirectAttributes redirectAttributes) {
        placeAdminService.deletePlace(placeId);
        redirectAttributes.addFlashAttribute("message", "장소가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/places";
    }

    @GetMapping("/search")
    public String searchPlace(@RequestParam String keyword) {
        PlaceResponseDto.PlaceDto place = placeAdminService.searchPlace(keyword);
        return "place";
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePlace(@RequestBody PlaceRequestDto.PlaceDto requestDto) {
        Place place = placeAdminService.savePlace(requestDto);
        return ResponseEntity.ok(place);
    }
}
