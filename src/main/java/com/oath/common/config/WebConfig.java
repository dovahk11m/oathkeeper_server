package com.oath.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String WEB_PATH = "/profile-images/**";

    private final String RESOURCE_PATH = "file:./uploads/profile/";

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler(WEB_PATH)
                .addResourceLocations(RESOURCE_PATH);
    }
}
