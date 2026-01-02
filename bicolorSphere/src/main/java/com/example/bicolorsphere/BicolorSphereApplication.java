package com.example.bicolorsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BicolorSphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(BicolorSphereApplication.class, args);
    }
}
