package com.oath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling // TaskScheduler 사용을 위해 추가
@EnableRetry      // @Retryable 사용을 위해 추가
@SpringBootApplication
public class OathApplication {

    public static void main(String[] args) {
        SpringApplication.run(OathApplication.class, args);
    }

}
