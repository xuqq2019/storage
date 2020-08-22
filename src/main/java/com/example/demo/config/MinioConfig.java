package com.example.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String minioUrl;
    @Value("${minio.access.key}")
    private String minioAccessKey;
    @Value("${minio.secret.key}")
    private String minioSecretKey;

    @Bean
    public MinioClient minioClient(){
        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        return MinioClient.builder().endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}
