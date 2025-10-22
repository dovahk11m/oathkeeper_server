package com.oath.common.config;

import com.oath.common.auth.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    // 전역 CORS 설정 (웹에서만 사용하는 설정)
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // 모든 경로에 대해
//                .allowedOrigins("http://localhost:3000") // 허용할 오리진
//                .allowedMethods("*") // 모든 HTTP 메서드 허용
//                .allowedHeaders("*") // 모든 헤더 허용
//                .allowCredentials(true); // 쿠키/인증 정보 허용
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // AuthInterceptor가 필요한 경로들을 명시적으로 추가
                .addPathPatterns(
                        // ===== Post (게시글) =====
                        "/posts/save",
                        // 글쓰기 페이지
                        "/posts/save",
                        // 글쓰기 처리
                        "/posts/update/**",
                        // 글 수정 페이지
                        "/posts/update/**",
                        // 글 수정 처리
                        "/posts/delete/**",
                        // 글 삭제 처리

                        // ===== Member (회원) =====
                        "/members/logout",
                        "/members/update-form",
                        "/members/update-nickname",
                        "/members/update-password",

                        // ===== Chat (채팅) =====
                        "/api/chats"
                        // 채팅 내역 불러오기
                );
    }
}
