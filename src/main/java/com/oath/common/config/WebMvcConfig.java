package com.oath.common.config;

import com.oath.common.auth.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

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
