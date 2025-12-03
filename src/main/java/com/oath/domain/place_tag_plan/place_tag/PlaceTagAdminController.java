//package com.oath.domain.place_tag_plan.place_tag;
//
//import com.oath.domain.place_tag_plan.place.Place;
//import com.oath.domain.place_tag_plan.place.PlaceAdminService;
//import com.oath.domain.place_tag_plan.tag.Tag;
//import com.oath.domain.place_tag_plan.tag.TagAdminService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/admin/place-tags")
//public class PlaceTagAdminController {
//
//    private final PlaceTagAdminService placeTagAdminService;
//    private final PlaceAdminService placeAdminService; // Place 목록을 가져오기 위해 추가
//    private final TagAdminService tagAdminService;     // Tag 목록을 가져오기 위해 추가
//
//    // 장소-태그 관계 목록 페이지 표시
//    @GetMapping
//    public String listPlaceTags(Model model) {
//        List<PlaceTag> placeTags = placeTagAdminService.findAllPlaceTags();
//        model.addAttribute("placeTags", placeTags);
//
//        // 연결 폼을 위한 장소와 태그 목록도 함께 전달
//        List<Place> places = placeAdminService.findAllPlaces();
//        List<Tag> tags = tagAdminService.findAllTags();
//        model.addAttribute("places", places);
//        model.addAttribute("tags", tags);
//        model.addAttribute("connectDto", new PlaceTagRequestDto.ConnectDto()); // 연결 폼 바인딩 객체
//
//        return "admin/place-tags/list"; // templates/admin/place-tags/list.html (or .mustache) 뷰를 반환
//    }
//
//    // 장소와 태그 연결 처리
//    @PostMapping
//    public String connectPlaceAndTag(PlaceTagRequestDto.ConnectDto requestDto, RedirectAttributes redirectAttributes) {
//        try {
//            placeTagAdminService.connectPlaceAndTag(requestDto);
//            redirectAttributes.addFlashAttribute("message", "장소와 태그가 성공적으로 연결되었습니다.");
//        } catch (IllegalStateException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        }
//        return "redirect:/admin/place-tags";
//    }
//
//    // 장소와 태그 연결 해제 처리
//    @PostMapping("/disconnect") // DELETE 대신 POST를 사용하여 폼 제출 처리
//    public String disconnectPlaceAndTag(PlaceTagRequestDto.ConnectDto requestDto, RedirectAttributes redirectAttributes) {
//        try {
//            placeTagAdminService.disconnectPlaceAndTag(requestDto);
//            redirectAttributes.addFlashAttribute("message", "장소와 태그 연결이 성공적으로 해제되었습니다.");
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        }
//        return "redirect:/admin/place-tags";
//    }
//}
