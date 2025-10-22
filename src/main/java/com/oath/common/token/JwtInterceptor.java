package com.oath.common.token;

import com.oath.domain.members.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtTokenProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        //1.요청메시지 헤더에서 키값 Authorization 헤더를 찾아 JWT 토큰을 추출
        //2.순수 토큰 추출
        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            //true && true

            //request 이메일, memberRole 넘겨주기
            String memberEmail = jwtTokenProvider.getSubject(token);
            MemberRole memberRole = jwtTokenProvider.getRole(token);
            request.setAttribute(
                    "memberEmail",
                    memberEmail
            );
            request.setAttribute(
                    "memberRole",
                    memberRole
            );

            return true;
        }
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "유효하지 않은 토큰"
        );
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
