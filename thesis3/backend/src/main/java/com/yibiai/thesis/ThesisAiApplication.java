package com.yibiai.thesis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ThesisAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThesisAiApplication.class, args);
    }
}
