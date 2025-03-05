package com.example.demo.config;

import com.example.demo.cloud.CloudService;
import com.example.demo.cloud.ICloudService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Value("${cloud.subnet_id}")
    private String subnetId;
    @Value("${cloud.folder_id}")
    private String folderId;
    @Bean
    public ICloudService getCloudService()
    {
        return new CloudService(folderId, subnetId, null);
    }
}
