package com.exan.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exan.storage")
public class ExanStorageProperties {
    private String localUploadDir = "./exan-data/uploads";

    public String getLocalUploadDir() {
        return localUploadDir;
    }

    public void setLocalUploadDir(String localUploadDir) {
        this.localUploadDir = localUploadDir;
    }
}
