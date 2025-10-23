package com.oath.domain.members.controller;

import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.service.KakaoService;
import com.oath.domain.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    private final KakaoService kakaoService;

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto memberLoginDto){
        Member member = memberService.login(memberLoginDto);

        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getSub());
        if(originalMember == null){
            originalMember = memberService.createOauth(kakaoProfileDto.getSub(), kakaoProfileDto.getEmail(), SocialType.KAKAO);
        }
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

}
