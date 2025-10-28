package com.oath.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "upload")
@Data
public class UploadConfig {
    private String rootDir;
    private String chatDir;
    private String chatFileDir;
    private String profileDir;
}