package com.oath.common;

import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.members.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(basePackages = "com.oath.domain.members")
@RequiredArgsConstructor
public class AdminModelAdvice {

    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;


    @ModelAttribute
    public void addAdminName(Model model, HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getMemberId(token); // JWT에서 userId 추출
            Member admin = memberRepository.findById(userId).orElseThrow();// DB에서 Admin 조회
            model.addAttribute("adminName", admin.getUsername());
        }
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        return java.util.Arrays.stream(request.getCookies())
                .filter(c -> "accessToken".equals(c.getName())).map(c -> c.getValue()).findFirst()
                .orElse(null);
    }
}
