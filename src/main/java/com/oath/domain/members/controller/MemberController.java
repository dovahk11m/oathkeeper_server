package com.oath.domain.members.controller;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.oath.common.CommonResponse;
import com.oath.common.JwtTokenProvider;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception404;
import com.oath.document.MemberApiResponseExamples;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.AccessTokenDto;
import com.oath.domain.members.dto.FacebookProfileDto;
import com.oath.domain.members.dto.KakaoProfileDto;
import com.oath.domain.members.dto.MemberCreateDto;
import com.oath.domain.members.dto.MemberLoginDto;
import com.oath.domain.members.dto.MemberRequest;
import com.oath.domain.members.dto.MemberResponse;
import com.oath.domain.members.service.FacebookService;
import com.oath.domain.members.service.KakaoService;
import com.oath.domain.members.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class MemberController {
        private final MemberService memberService;
        private final KakaoService kakaoService;
        private final FacebookService facebookService;
        private final JwtTokenProvider jwtTokenProvider;

        @Operation(summary = "회원가입", description = "새로운 회원을 등록하고 이메일 인증을 요청합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "회원가입 성공",
                        content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = CommonResponse.class),
                                        examples = @ExampleObject(
                                                        value = MemberApiResponseExamples.MemberCreate.SUCCESS_201))),
                        @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(
                                                                        implementation = CommonResponse.class),
                                                        examples = @ExampleObject(
                                                                        value = MemberApiResponseExamples.MemberCreate.CONFLICT_409)))})
        @PostMapping("/create")
        public ResponseEntity<CommonResponse<?>> memberCreate(
                        @RequestBody MemberCreateDto memberCreateDto) {
                Member member = memberService.create(memberCreateDto);
                return new ResponseEntity<>(
                                CommonResponse.success(member.getId(), "회원가입 성공. 이메일 인증을 완료해주세요."),
                                HttpStatus.CREATED);
        }

        @Operation(summary = "이메일 인증", description = "회원가입 후 발송된 이메일의 링크를 통해 계정을 인증합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
                        @ApiResponse(responseCode = "400", description = "인증 토큰 만료"),
                        @ApiResponse(responseCode = "404", description = "유효하지 않은 인증 토큰")})
        @GetMapping("/verify")
        public ResponseEntity<String> verifyEmail(@Parameter(description = "이메일 인증 토큰",
                        required = true) @RequestParam("token") String token) {
                try {
                        memberService.verifyEmail(token);

                        // 딥링크를 통해 앱으로 리다이렉트
                        String deepLink = "oath://verification-success";

                        String htmlResponse = "<!DOCTYPE html>" + "<html lang='ko'>" + "<head>"
                                        + "    <meta charset='UTF-8'>"
                                        + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                                        + "    <title>이메일 인증 완료</title>" + "    <style>"
                                        + "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; "
                                        + "               text-align: center; padding: 50px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); "
                                        + "               color: white; margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center; }"
                                        + "        .container { background: white; color: #333; padding: 40px; border-radius: 20px; "
                                        + "                     box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; }"
                                        + "        h1 { color: #667eea; margin-bottom: 20px; font-size: 2em; }"
                                        + "        p { line-height: 1.6; margin: 15px 0; }"
                                        + "        .success-icon { font-size: 4em; margin-bottom: 20px; }"
                                        + "        .btn { display: inline-block; margin-top: 20px; padding: 15px 30px; "
                                        + "               background: #667eea; color: white; text-decoration: none; "
                                        + "               border-radius: 10px; font-weight: bold; transition: all 0.3s; }"
                                        + "        .btn:hover { background: #5568d3; transform: translateY(-2px); }"
                                        + "        .info { font-size: 0.9em; color: #666; margin-top: 30px; }"
                                        + "    </style>" + "    <script>"
                                        + "        window.onload = function() {"
                                        + "            setTimeout(function() {"
                                        + "                window.location.href = '" + deepLink
                                        + "';" + "            }, 1000);" + "        };"
                                        + "    </script>" + "</head>" + "<body>"
                                        + "    <div class='container'>"
                                        + "        <div class='success-icon'>✅</div>"
                                        + "        <h1>인증 완료!</h1>"
                                        + "        <p>이메일 인증이 성공적으로 완료되었습니다.</p>"
                                        + "        <p>잠시 후 자동으로 앱이 실행됩니다.</p>" + "        <a href='"
                                        + deepLink + "' class='btn'>앱으로 돌아가기</a>"
                                        + "        <p class='info'>앱이 자동으로 열리지 않으면 위 버튼을 클릭해주세요.</p>"
                                        + "    </div>" + "</body>" + "</html>";

                        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML)
                                        .body(htmlResponse);

                } catch (Exception400 e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .contentType(MediaType.TEXT_HTML)
                                        .body(createErrorPage("인증 토큰이 만료되었습니다", "다시 가입을 진행해주세요."));
                } catch (Exception404 e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.TEXT_HTML).body(createErrorPage(
                                                        "유효하지 않은 인증 링크입니다", "링크를 다시 확인해주세요."));
                }
        }

        @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "로그인 성공",
                        content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = CommonResponse.class),
                                        examples = @ExampleObject(
                                                        value = MemberApiResponseExamples.MemberLogin.SUCCESS_200))),
                        @ApiResponse(responseCode = "401", description = "이메일/비밀번호 불일치 또는 미인증 계정",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(
                                                                        implementation = CommonResponse.class),
                                                        examples = {@ExampleObject(
                                                                        name = "자격 증명 실패",
                                                                        value = MemberApiResponseExamples.MemberLogin.UNAUTHORIZED_401),
                                                                        @ExampleObject(name = "미인증 계정",
                                                                                        value = MemberApiResponseExamples.MemberLogin.INACTIVE_401)}))})
        @PostMapping("/login")
        public ResponseEntity<CommonResponse<?>> login(@RequestBody MemberLoginDto memberLoginDto) {
                Member member = memberService.login(memberLoginDto);
                String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole(),
                                member.getId());
                MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, member);
                return new ResponseEntity<>(CommonResponse.success(loginInfo, "로그인 성공"),
                                HttpStatus.OK);
        }

        @Operation(summary = "회원 정보 조회", description = "특정 회원의 상세 정보를 조회합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원 조회 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @GetMapping("/{memberId}")
        public ResponseEntity<CommonResponse<MemberResponse.DTO>> getMember(@Parameter(
                        description = "조회할 회원의 ID", required = true) @PathVariable Long memberId) {
                MemberResponse.DTO dto = memberService.getMember(memberId);
                return ResponseEntity.ok(CommonResponse.success(dto, "회원 조회 성공"));
        }

        @Operation(summary = "회원 정보 수정", description = "회원의 이름, 프로필 이미지, 주소 등을 수정합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @PutMapping("/{memberId}")
        public ResponseEntity<CommonResponse<MemberResponse.DTO>> updateMember(
                        @Parameter(description = "수정할 회원의 ID",
                                        required = true) @PathVariable Long memberId,
                        @RequestBody MemberRequest.Update request) {
                MemberResponse.DTO dto = memberService.updateMember(memberId, request);
                return ResponseEntity.ok(CommonResponse.success(dto, "회원정보 수정 성공"));
        }

        @Operation(summary = "비밀번호 수정", description = "회원의 비밀번호를 수정합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "비밀번호 수정 성공"),
                        @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @PatchMapping("/{memberId}/password")
        public ResponseEntity<CommonResponse<?>> updatePassword(
                        @Parameter(description = "비밀번호를 수정할 회원의 ID",
                                        required = true) @PathVariable Long memberId,
                        @RequestBody MemberRequest.PasswordUpdate request) {
                memberService.updatePassword(memberId, request);
                return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 수정 성공"));
        }

        @Operation(summary = "아이디 찾기", description = "이메일 정보를 통해 회원 아이디(username)를 찾습니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @PostMapping("/find-id")
        public ResponseEntity<CommonResponse<?>> findId(@RequestBody MemberRequest.FindId request) {
                String foundId = memberService.findId(request);
                return ResponseEntity.ok(CommonResponse.success(foundId, "아이디 찾기 성공"));
        }

        @Operation(summary = "비밀번호 찾기", description = "등록된 이메일로 임시 비밀번호를 발송합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "비밀번호 찾기용 메일 보내기 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @PostMapping("/find-password")
        public ResponseEntity<CommonResponse<?>> findPassword(
                        @RequestBody MemberRequest.FindPassword request) {
                memberService.sendTemporaryPassword(request);
                return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 찾기용 메일 보내기 성공"));
        }

        @Operation(summary = "회원 탈퇴", description = "회원 계정을 비활성화 상태로 변경합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @DeleteMapping("/{memberId}")
        public ResponseEntity<CommonResponse<?>> deleteMember(@Parameter(description = "탈퇴할 회원의 ID",
                        required = true) @PathVariable Long memberId) {
                memberService.deleteMember(memberId);
                return ResponseEntity.ok(CommonResponse.success(null, "회원 탈퇴 성공"));
        }

        @Operation(summary = "로그아웃", description = "현재 로그인된 세션의 JWT 토큰을 블랙리스트에 추가합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")})
        @PostMapping("/logout")
        public ResponseEntity<CommonResponse<?>> logout(HttpServletRequest request) {
                String token = jwtTokenProvider.resolveToken(request);
                memberService.logout(token);
                return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
        }

        @Auth
        @Operation(summary = "비밀번호 확인", description = "회원의 현재 비밀번호가 일치하는지 확인합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
                        @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @PostMapping("/{memberId}/check-password")
        public ResponseEntity<CommonResponse<?>> checkPassword(
                        @Parameter(description = "비밀번호를 확인할 회원의 ID",
                                        required = true) @PathVariable Long memberId,
                        @RequestBody MemberRequest.CheckPassword request) {
                boolean isPasswordCorrect =
                                memberService.checkPassword(memberId, request.getPassword());
                if (isPasswordCorrect) {
                        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 확인 성공"));
                } else {
                        throw new Exception401("비밀번호가 일치하지 않습니다.");
                }
        }

        @Operation(summary = "카카오 로그인 (SDK용)",
                        description = "모바일 SDK에서 발급받은 카카오 액세스 토큰으로 로그인 또는 회원가입을 처리합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "카카오 로그인 성공"),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 카카오 액세스 토큰"),
                        @ApiResponse(responseCode = "401", description = "계정 상태에 따른 로그인 제한"),
                        @ApiResponse(responseCode = "500", description = "카카오 서버 연동 오류")})
        @PostMapping("/kakao/token")
        public ResponseEntity<CommonResponse<?>> kakaoLoginWithToken(
                        @RequestBody AccessTokenDto accessTokenDto) {

                if (accessTokenDto.getAccess_token() == null
                                || accessTokenDto.getAccess_token().isBlank()) {
                        throw new Exception400("access_token이 필요합니다.");
                }

                KakaoProfileDto kakaoProfileDto =
                                kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

                Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());

                if (originalMember == null) {
                        String nickname = "사용자";
                        String email = null;

                        if (kakaoProfileDto.getKakao_account() != null) {
                                email = kakaoProfileDto.getKakao_account().getEmail();
                                if (kakaoProfileDto.getKakao_account().getProfile() != null
                                                && kakaoProfileDto.getKakao_account().getProfile()
                                                                .getNickname() != null) {
                                        nickname = kakaoProfileDto.getKakao_account().getProfile()
                                                        .getNickname();
                                }
                        }

                        if (email == null || email.isBlank()) {
                                email = kakaoProfileDto.getId() + "@kakao.oath.com";
                        }

                        originalMember = memberService.createOauth(kakaoProfileDto.getId(), email,
                                        SocialType.KAKAO, nickname);
                }

                memberService.postLogin(originalMember);

                String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(),
                                originalMember.getRole(), originalMember.getId());

                MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);

                return ResponseEntity.ok(CommonResponse.success(loginInfo, "카카오 로그인 성공"));
        }

        @Operation(summary = "페이스북 로그인", description = "페이스북 OAuth를 통한 로그인 또는 회원가입을 처리합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "페이스북 로그인 성공"),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 페이스북 액세스 토큰"),
                        @ApiResponse(responseCode = "401", description = "계정 상태에 따른 로그인 제한"),
                        @ApiResponse(responseCode = "500", description = "페이스북 서버 연동 오류")})
        @PostMapping("/facebook/doLogin")
        public ResponseEntity<CommonResponse<?>> facebookLogin(
                        @RequestBody AccessTokenDto accessTokenDto) {
                FacebookProfileDto facebookProfileDto = facebookService
                                .getFacebookProfile(accessTokenDto.getAccess_token());
                Member originalMember =
                                memberService.getMemberBySocialId(facebookProfileDto.getId());
                if (originalMember == null) {
                        String email = facebookProfileDto.getEmail();
                        if (email == null || email.isBlank()) {
                                email = facebookProfileDto.getId() + "@facebook.oath.com";
                        }

                        originalMember = memberService.createOauth(facebookProfileDto.getId(),
                                        email, SocialType.FACEBOOK, facebookProfileDto.getName());
                }
                memberService.postLogin(originalMember);
                String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(),
                                originalMember.getRole(), originalMember.getId());
                MemberResponse.Login loginInfo = new MemberResponse.Login(jwtToken, originalMember);
                return new ResponseEntity<>(CommonResponse.success(loginInfo, "페이스북 로그인 성공"),
                                HttpStatus.OK);
        }

        @Operation(summary = "프로필 이미지 업로드", description = "회원의 프로필 이미지를 업로드합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
                        @ApiResponse(responseCode = "500", description = "파일 업로드 중 서버 오류")})
        @PostMapping("/profile/upload/{memberId}")
        public ResponseEntity<?> uploadProfileImage(
                        @Parameter(description = "업로드할 프로필 이미지 파일", required = true) @RequestParam(
                                        name = "image") MultipartFile image,
                        @Parameter(description = "이미지를 업로드할 회원의 ID", required = true) @PathVariable(
                                        name = "memberId") Long memberId) {
                try {
                        String imageUrl = memberService.uploadProfileImage(image, memberId);
                        return new ResponseEntity<>(CommonResponse.success(imageUrl),
                                        HttpStatus.OK);
                } catch (IOException e) {
                        return new ResponseEntity<>(CommonResponse.error(e.getMessage()),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Operation(summary = "프로필 이미지 삭제", description = "회원의 프로필 이미지를 삭제합니다.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "프로필 이미지 삭제 성공"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
        @DeleteMapping("/profile/delete/{memberId}")
        public ResponseEntity<CommonResponse<?>> deleteProfileImage(
                        @Parameter(description = "이미지를 삭제할 회원의 ID",
                                        required = true) @PathVariable Long memberId) {
                memberService.deleteProfileImage(memberId);
                return ResponseEntity.ok(CommonResponse.success(null, "프로필 이미지 삭제 성공"));
        }

        @Auth(roles = Role.ADMIN)
        @Operation(summary = "회원 가입 수 카운트", description = "현재까지 가입된 회원 수를 카운트합니다. (관리자용)")
        @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원 가입 수 카운트 성공"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")})
        @GetMapping("/count-join")
        public ResponseEntity<CommonResponse<?>> countJoin() {
                memberService.countJoin();
                return ResponseEntity.ok(CommonResponse.success(null, "회원 가입 수 카운트 성공"));
        }

        private String createErrorPage(String title, String message) {
                return "<!DOCTYPE html>" + "<html lang='ko'>" + "<head>"
                                + "    <meta charset='UTF-8'>"
                                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                                + "    <title>인증 실패</title>" + "    <style>"
                                + "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; "
                                + "               text-align: center; padding: 50px 20px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); "
                                + "               color: white; margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center; }"
                                + "        .container { background: white; color: #333; padding: 40px; border-radius: 20px; "
                                + "                     box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; }"
                                + "        h1 { color: #f5576c; margin-bottom: 20px; font-size: 2em; }"
                                + "        p { line-height: 1.6; margin: 15px 0; }"
                                + "        .error-icon { font-size: 4em; margin-bottom: 20px; }"
                                + "    </style>" + "</head>" + "<body>"
                                + "    <div class='container'>"
                                + "        <div class='error-icon'>❌</div>" + "        <h1>" + title
                                + "</h1>" + "        <p>" + message + "</p>" + "    </div>"
                                + "</body>" + "</html>";
        }
}
