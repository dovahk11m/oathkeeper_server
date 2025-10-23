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
                // /api/ 로 시작하는 모든 경로에 인터셉터를 적용합니다.
                .addPathPatterns("/api/**")
                // 단, 로그인, 회원가입 등 인증이 필요 없는 경로는 제외합니다.
                .excludePathPatterns(
                        "/api/member/create",
                        "/api/member/login",
                        "/api/member/kakao/login"
                        // TODO: 소셜 로그인 관련 경로도 필요 시 추가
                );
    }
}
