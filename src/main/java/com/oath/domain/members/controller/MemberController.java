package com.oath.domain.members.controller;

import com.oath.common.CommonResponse;
import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.service.FacebookService;
import com.oath.domain.members.service.KakaoService;
import com.oath.domain.members.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, member);
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

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        memberService.logout(token);
        return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
    }

    /** 비밀번호 확인 */
    @PostMapping("/{memberId}/check-password")
    public ResponseEntity<?> checkPassword(@PathVariable Long memberId, @RequestBody MemberRequest.CheckPassword request) {
        boolean isPasswordCorrect = memberService.checkPassword(memberId, request.getPassword());
        if (isPasswordCorrect) {
            return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 확인 성공"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponse.error("비밀번호가 일치하지 않습니다."));
        }
    }


    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto =
                kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());
        if(originalMember == null){
            originalMember = memberService.createOauth(kakaoProfileDto.getId(), kakaoProfileDto.getKakao_account().getEmail(), SocialType.KAKAO, kakaoProfileDto.getKakao_account().getProfile().getNickname());
        }

        memberService.postLogin(originalMember);

        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole(), originalMember.getId());

        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/facebook/doLogin")
    public ResponseEntity<?> facebookLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto =
                facebookService.getAccessToken(redirectDto.getCode());
        FacebookProfileDto facebookProfileDto =
                facebookService.getFacebookProfile(accessTokenDto.getAccess_token());

        Member originalMember = memberService.getMemberBySocialId(facebookProfileDto.getId());
        if(originalMember == null){
            originalMember = memberService.createOauth(facebookProfileDto.getId(), facebookProfileDto.getEmail(), SocialType.FACEBOOK, facebookProfileDto.getName());
        }

        memberService.postLogin(originalMember);

        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole(), originalMember.getId());

        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/profile/upload/{memberId}")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image, @PathVariable Long memberId) {

        try {
            String imageUrl = memberService.uploadProfileImage(image, memberId);
            return new ResponseEntity<>(CommonResponse.success(imageUrl), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(CommonResponse.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/profile/delete/{memberId}")
    public void deleteProfileImage (@PathVariable Long memberId) {
        memberService.deleteProfileImage(memberId);
    }

    @GetMapping("/count-join")
    public void countJoin () {
        memberService.countJoin();
    }

}
