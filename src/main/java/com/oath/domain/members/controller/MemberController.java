package com.oath.domain.members.controller;

import com.oath.common.CommonResponse;
import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.service.FacebookService;
import com.oath.domain.members.service.KakaoService;
import com.oath.domain.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    private final KakaoService kakaoService;

    private final FacebookService facebookService;

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginDto memberLoginDto){
        Member member = memberService.login(memberLoginDto);

        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole(), member.getId());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    /** 회원 정보 조회 */
    @GetMapping("/{memberId}")
    public ResponseEntity<CommonResponse<MemberResponse.DTO>> getMember(@PathVariable Long memberId) {
        MemberResponse.DTO dto = memberService.getMember(memberId);
        return ResponseEntity.ok(CommonResponse.success(dto, "회원 조회 성공"));
    }

    /** 회원 정보 수정 (이름, 이메일 등) */
    @PutMapping("/{memberId}")
    public ResponseEntity<CommonResponse<MemberResponse.DTO>> updateMember(
            @PathVariable Long memberId,
            @RequestBody MemberRequest.Update request) {
        MemberResponse.DTO dto = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(CommonResponse.success(dto, "회원정보 수정 성공"));
    }

    /** 비밀번호 수정 */
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long memberId,
            @RequestBody MemberRequest.PasswordUpdate request) {
        memberService.updatePassword(memberId, request);
        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 수정 성공"));
    }

    /** 아이디 찾기 */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody MemberRequest.FindId request) {
        try {
            String foundId = memberService.findId(request);
            return ResponseEntity.ok(CommonResponse.success(foundId, "아이디 찾기 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.success(null, e.getMessage()));
        }
    }

    /** 비밀번호 찾기 (임시 비밀번호 발급) */
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@RequestBody MemberRequest.FindPassword request) {
        memberService.sendTemporaryPassword(request);
        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 찾기용 메일 보내기 성공"));
    }

    /** 회원 탈퇴 */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);

        return ResponseEntity.ok(CommonResponse.success(null, "회원 탈퇴 성공"));
    }


    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto =
                kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        System.out.println("로그인한 카카오 프로필: " + kakaoProfileDto);
        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());
        if(originalMember == null){
            originalMember = memberService.createOauth(kakaoProfileDto.getId(), kakaoProfileDto.getKakao_account().getEmail(), SocialType.KAKAO, kakaoProfileDto.getKakao_account().getProfile().getNickname());
        }
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole(), originalMember.getId());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/facebook/doLogin")
    public ResponseEntity<?> facebookLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto =
                facebookService.getAccessToken(redirectDto.getCode());
        FacebookProfileDto facebookProfileDto =
                facebookService.getFacebookProfile(accessTokenDto.getAccess_token());
        System.out.println("로그인한 페이스북 프로필: " + facebookProfileDto);
        Member originalMember = memberService.getMemberBySocialId(facebookProfileDto.getId());
        if(originalMember == null){
            originalMember = memberService.createOauth(facebookProfileDto.getId(), facebookProfileDto.getEmail(), SocialType.FACEBOOK, facebookProfileDto.getName());
        }
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole(), originalMember.getId());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

}
