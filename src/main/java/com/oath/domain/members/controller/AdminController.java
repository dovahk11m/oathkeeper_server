package com.oath.domain.members.controller;

import com.oath.common.JwtTokenProvider;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception401;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.service.MemberService;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.visitors.VisitorService;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import com.oath.common.CommonResponse;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminRequest;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.members.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final MemberService memberService;

    private final JwtTokenProvider jwtTokenProvider;

    private final VisitorService visitorService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<CommonResponse<Object>> adminLogin(@RequestBody MemberLoginDto dto) {
        Member member = memberService.login(dto); // 공통 로그인 사용
        checkAdmin(member);

        String token = jwtTokenProvider.createToken(
                member.getEmail(),
                member.getRole(),
                member.getId()
        );

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(CommonResponse.success(
                        Map.of(
                            "memberId", member.getId(),
                            "email", member.getEmail(),
                            "role", member.getRole()
                                ),
                        "로그인 성공"
                ));
    }

    private void checkAdmin(Member member) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new Exception401("관리자 권한이 없습니다.");
        }
    }

    @Auth
    @PostMapping("/chat-access/verify")
    public ResponseEntity<CommonResponse<Object>> checkPassword(
            @CookieValue("accessToken") String token,
            @RequestBody Map<String, String> req) {

        if(token == null || token.isEmpty()) {
            return new ResponseEntity<>(CommonResponse.error("로그인 필요"), HttpStatus.UNAUTHORIZED);
        }

        Long adminId = jwtTokenProvider.getMemberId(token); // JWT에서 ID 추출
        String password = req.get("password");
        Long groupId = Long.valueOf(req.get("groupId"));

        boolean valid = memberService.checkPassword(adminId, password);

        visitorService.saveVisitor(groupId, adminId);

        return ResponseEntity.ok(CommonResponse.success(valid));
    }

    @Auth
    @GetMapping("/tags")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<String>>> getTagList() {
        List<String> tags = adminService.getTagList();
        return ResponseEntity.ok(CommonResponse.success(tags));
    }

    @Auth
    @PostMapping("/tags")
    public ResponseEntity<CommonResponse<Object>> addTag(@RequestBody AdminRequest.TagRequest req) {
        adminService.addTag(req.getName());
        return ResponseEntity.ok(CommonResponse.success(null, "태그가 추가되었습니다."));
    }

    @Auth
    @DeleteMapping("/tags/{name}")
    public ResponseEntity<CommonResponse<Object>> deleteTag(@PathVariable String name) {
        adminService.deleteTag(name);
        return ResponseEntity.ok(CommonResponse.success(null, "태그가 삭제되었습니다."));
    }

    @Auth
    @GetMapping("/places")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.placeList>>> getPlaceList() {
        List<AdminResponse.placeList> places = adminService.getPlaceList();
        return ResponseEntity.ok(CommonResponse.success(places));
    }

    @Auth
    @PostMapping("/places")
    public ResponseEntity<CommonResponse<Place>> savePlace(@RequestBody AdminRequest.PlaceDto requestDto) {
        Place place = adminService.savePlace(requestDto);
        return ResponseEntity.ok(CommonResponse.success(place));
    }

    @Auth
    @DeleteMapping("/places/{placeId}")
    public ResponseEntity<CommonResponse<Object>> deletePlace(@PathVariable Long placeId) {
        adminService.deletePlace(placeId);
        return ResponseEntity.ok(CommonResponse.success(null, "장소가 삭제되었습니다."));
    }

    @Auth
    @PostMapping("/places/{placeId}/description")
    public ResponseEntity<CommonResponse<Object>> updateDescription (@PathVariable Long placeId, @RequestBody AdminRequest.updateDescription req) {
        adminService.updateDescription(placeId, req);
        return ResponseEntity.ok(CommonResponse.success(null, "장소 상세가 수정되었습니다."));
    }

    @Auth
    @GetMapping("/places/tags")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlaceTag>>> getPlaceTag() {
        List<AdminResponse.PlaceTag> placeTags = adminService.getPlaceTag();
        return ResponseEntity.ok(CommonResponse.success(placeTags));
    }

    @Auth
    @PostMapping("/places/tags")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.AddedTagDto>>> addPlaceTag(@RequestBody AdminRequest.PlaceTag req) {
        List<AdminResponse.AddedTagDto> tagDto = adminService.addPlaceTag(req);
        return ResponseEntity.ok(CommonResponse.success(tagDto));
    }

    @Auth
    @DeleteMapping("/places/{placeId}/tags/{tagId}")
    @ResponseBody
    public ResponseEntity<CommonResponse<Object>> deletePlaceTag(@PathVariable Long placeId, @PathVariable Long tagId) {
        adminService.deletePlaceTag(placeId, tagId);
        return ResponseEntity.ok(CommonResponse.success(null, "장소-태그가 삭제되었습니다."));
    }

    @Auth
    @GetMapping("/plan-count")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlanCount>>> getPlanCount() {
        List<AdminResponse.PlanCount> planCount = adminService.getPlanCount();
        return ResponseEntity.ok(CommonResponse.success(planCount));
    }

    @Auth
    @GetMapping("/par-count")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.ParticipantCount>>> getParticipantCount() {
        List<AdminResponse.ParticipantCount> participantCount = adminService.getParticipantCount();
        return ResponseEntity.ok(CommonResponse.success(participantCount));
    }

    @Auth
    @GetMapping("/plan-tag-pie")
    @ResponseBody
    public ResponseEntity<CommonResponse<List<AdminResponse.PlanTagPie>>> PlanTagPie() {
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminService.PlanTagPie();
        return ResponseEntity.ok(CommonResponse.success(PlanTagsPie));
    }


}
