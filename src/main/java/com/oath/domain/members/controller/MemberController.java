package com.oath.domain.members.controller;

import com.oath.common.CommonResponse;
import com.oath.common.JwtTokenProvider;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception401;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.service.FacebookService;
import com.oath.domain.members.service.KakaoService;
import com.oath.domain.members.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final KakaoService kakaoService;
    private final FacebookService facebookService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록하고 이메일 인증을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    @PostMapping("/create")
    public ResponseEntity<CommonResponse<?>> memberCreate(
            @Parameter(description = "회원가입 요청 정보", required = true) @RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(CommonResponse.success(member.getId(), "회원가입 성공. 이메일 인증을 완료해주세요."), HttpStatus.CREATED);
    }

    @Operation(summary = "이메일 인증", description = "회원가입 후 발송된 이메일의 링크를 통해 계정을 인증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 토큰 만료"),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 인증 토큰")
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "이메일 인증 토큰", required = true) @RequestParam("token") String token) {
        memberService.verifyEmail(token);
        String htmlResponse = "<html><body style='text-align:center; padding-top:50px; font-family:sans-serif;'>"
                + "<h1>인증 완료</h1>"
                + "<p>이메일 인증이 성공적으로 완료되었습니다.</p>"
                + "<p>이제 앱으로 돌아가 로그인을 진행해주세요.</p>"
                + "</body></html>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlResponse);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "이메일/비밀번호 불일치, 미인증 계정, 휴면 계정 등")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<?>> login(
            @Parameter(description = "로그인 요청 정보", required = true) @RequestBody MemberLoginDto memberLoginDto) {
        Member member = memberService.login(memberLoginDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole(), member.getId());
        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, member);
        return new ResponseEntity<>(CommonResponse.success(loginInfo, "로그인 성공"), HttpStatus.OK);
    }

    @Operation(summary = "회원 정보 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<CommonResponse<MemberResponse.DTO>> getMember(
            @Parameter(description = "조회할 회원의 ID", required = true) @PathVariable Long memberId) {
        MemberResponse.DTO dto = memberService.getMember(memberId);
        return ResponseEntity.ok(CommonResponse.success(dto, "회원 조회 성공"));
    }

    @Operation(summary = "회원 정보 수정", description = "회원의 이름, 프로필 이미지, 주소 등을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원정보 수정 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PutMapping("/{memberId}")
    public ResponseEntity<CommonResponse<MemberResponse.DTO>> updateMember(
            @Parameter(description = "수정할 회원의 ID", required = true) @PathVariable Long memberId,
            @Parameter(description = "회원 정보 수정 요청", required = true) @RequestBody MemberRequest.Update request) {
        MemberResponse.DTO dto = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(CommonResponse.success(dto, "회원정보 수정 성공"));
    }

    @Operation(summary = "비밀번호 수정", description = "회원의 비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<CommonResponse<?>> updatePassword(
            @Parameter(description = "비밀번호를 수정할 회원의 ID", required = true) @PathVariable Long memberId,
            @Parameter(description = "비밀번호 수정 요청 정보", required = true) @RequestBody MemberRequest.PasswordUpdate request) {
        memberService.updatePassword(memberId, request);
        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 수정 성공"));
    }

    @Operation(summary = "아이디 찾기", description = "이메일 정보를 통해 회원 아이디(username)를 찾습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PostMapping("/find-id")
    public ResponseEntity<CommonResponse<?>> findId(
            @Parameter(description = "아이디 찾기 요청 정보", required = true) @RequestBody MemberRequest.FindId request) {
        String foundId = memberService.findId(request);
        return ResponseEntity.ok(CommonResponse.success(foundId, "아이디 찾기 성공"));
    }

    @Operation(summary = "비밀번호 찾기", description = "등록된 이메일로 임시 비밀번호를 발송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 찾기용 메일 보내기 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PostMapping("/find-password")
    public ResponseEntity<CommonResponse<?>> findPassword(
            @Parameter(description = "비밀번호 찾기 요청 정보", required = true) @RequestBody MemberRequest.FindPassword request) {
        memberService.sendTemporaryPassword(request);
        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 찾기용 메일 보내기 성공"));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 계정을 비활성화 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @DeleteMapping("/{memberId}")
    public ResponseEntity<CommonResponse<?>> deleteMember(
            @Parameter(description = "탈퇴할 회원의 ID", required = true) @PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(CommonResponse.success(null, "회원 탈퇴 성공"));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 세션의 JWT 토큰을 블랙리스트에 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<?>> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        memberService.logout(token);
        return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
    }

    @Auth
    @Operation(summary = "비밀번호 확인", description = "회원의 현재 비밀번호가 일치하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PostMapping("/{memberId}/check-password")
    public ResponseEntity<CommonResponse<?>> checkPassword(
            @Parameter(description = "비밀번호를 확인할 회원의 ID", required = true) @PathVariable Long memberId,
            @Parameter(description = "비밀번호 확인 요청 정보", required = true) @RequestBody MemberRequest.CheckPassword request) {
        boolean isPasswordCorrect = memberService.checkPassword(memberId, request.getPassword());
        if (isPasswordCorrect) {
            return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 확인 성공"));
        } else {
            throw new Exception401("비밀번호가 일치하지 않습니다.");
        }
    }

    @Operation(summary = "카카오 로그인 (SDK용)", description = "모바일 SDK에서 발급받은 카카오 액세스 토큰으로 로그인 또는 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 카카오 액세스 토큰"),
            @ApiResponse(responseCode = "401", description = "계정 상태에 따른 로그인 제한"),
            @ApiResponse(responseCode = "500", description = "카카오 서버 연동 오류")
    })
    @PostMapping("/kakao/token")
    public ResponseEntity<CommonResponse<?>> kakaoLoginWithToken(
            @Parameter(description = "카카오 SDK에서 발급받은 액세스 토큰", required = true)
            @RequestBody AccessTokenDto accessTokenDto) {

        if (accessTokenDto.getAccess_token() == null || accessTokenDto.getAccess_token().isBlank()) {
            throw new Exception400("access_token이 필요합니다.");
        }

        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());

        if (originalMember == null) {
            String nickname = "사용자";
            String email = null;

            if (kakaoProfileDto.getKakao_account() != null) {
                email = kakaoProfileDto.getKakao_account().getEmail();
                if (kakaoProfileDto.getKakao_account().getProfile() != null && kakaoProfileDto.getKakao_account().getProfile().getNickname() != null) {
                    nickname = kakaoProfileDto.getKakao_account().getProfile().getNickname();
                }
            }
            
            if (email == null || email.isBlank()) {
                email = kakaoProfileDto.getId() + "@kakao.oath.com";
            }

            originalMember = memberService.createOauth(
                    kakaoProfileDto.getId(),
                    email,
                    SocialType.KAKAO,
                    nickname
            );
        }

        memberService.postLogin(originalMember);

        String jwtToken = jwtTokenProvider.createToken(
                originalMember.getEmail(),
                originalMember.getRole(),
                originalMember.getId()
        );

        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);

        return ResponseEntity.ok(CommonResponse.success(loginInfo, "카카오 로그인 성공"));
    }

    @Operation(summary = "페이스북 로그인", description = "페이스북 OAuth를 통한 로그인 또는 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페이스북 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 페이스북 액세스 토큰"),
            @ApiResponse(responseCode = "401", description = "계정 상태에 따른 로그인 제한"),
            @ApiResponse(responseCode = "500", description = "페이스북 서버 연동 오류")
    })
    @PostMapping("/facebook/doLogin")
    public ResponseEntity<CommonResponse<?>> facebookLogin(
            @Parameter(description = "페이스북 액세스 토큰", required = true) @RequestBody AccessTokenDto accessTokenDto) {
        FacebookProfileDto facebookProfileDto = facebookService.getFacebookProfile(accessTokenDto.getAccess_token());
        Member originalMember = memberService.getMemberBySocialId(facebookProfileDto.getId());
        if (originalMember == null) {
            String email = facebookProfileDto.getEmail();
            if (email == null || email.isBlank()) {
                email = facebookProfileDto.getId() + "@facebook.oath.com";
            }
            
            originalMember = memberService.createOauth(
                    facebookProfileDto.getId(),
                    email,
                    SocialType.FACEBOOK,
                    facebookProfileDto.getName()
            );
        }
        memberService.postLogin(originalMember);
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole(), originalMember.getId());
        MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);
        return new ResponseEntity<>(CommonResponse.success(loginInfo, "페이스북 로그인 성공"), HttpStatus.OK);
    }

    @Operation(summary = "프로필 이미지 업로드", description = "회원의 프로필 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 중 서버 오류")
    })
    @PostMapping("/profile/upload/{memberId}")
    public ResponseEntity<?> uploadProfileImage(
            @Parameter(description = "업로드할 프로필 이미지 파일", required = true) @RequestParam("image") MultipartFile image,
            @Parameter(description = "이미지를 업로드할 회원의 ID", required = true) @PathVariable Long memberId) {
        try {
            String imageUrl = memberService.uploadProfileImage(image, memberId);
            return new ResponseEntity<>(CommonResponse.success(imageUrl), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(CommonResponse.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "프로필 이미지 삭제", description = "회원의 프로필 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @DeleteMapping("/profile/delete/{memberId}")
    public ResponseEntity<CommonResponse<?>> deleteProfileImage(
            @Parameter(description = "이미지를 삭제할 회원의 ID", required = true) @PathVariable Long memberId) {
        memberService.deleteProfileImage(memberId);
        return ResponseEntity.ok(CommonResponse.success(null, "프로필 이미지 삭제 성공"));
    }

    @Auth(roles = Role.ADMIN)
    @Operation(summary = "회원 가입 수 카운트", description = "현재까지 가입된 회원 수를 카운트합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 가입 수 카운트 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/count-join")
    public ResponseEntity<CommonResponse<?>> countJoin() {
        memberService.countJoin();
        return ResponseEntity.ok(CommonResponse.success(null, "회원 가입 수 카운트 성공"));
    }
}
