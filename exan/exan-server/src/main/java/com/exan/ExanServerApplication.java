package com.exan;

import com.exan.infra.config.ExanStorageProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.exan.domain.mapper")
@EnableConfigurationProperties(ExanStorageProperties.class)
public class ExanServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExanServerApplication.class, args);
    }
}
