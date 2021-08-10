package com.xqq.oss.config;

import com.xqq.oss.condition.MinIoCondition;
import com.xqq.oss.service.ObjectStorageSystemService;
import com.xqq.oss.service.impl.MinIoServiceImpl;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * minio的OSS连接配置
 * @author xuqq
 * @date 2020-12-28
 * @version v1.0
 */
@Configuration
@Conditional(MinIoCondition.class)
public class MinIoOssConfig {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.access.key.id}")
    private String accessKeyId;
    @Value("${oss.access.key.secret}")
    private String accessKeySecret;

    @Bean
    public MinioClient minioClient(){
        System.out.println("start minio OSS");
        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        return MinioClient.builder().endpoint(endpoint)
                .credentials(accessKeyId, accessKeySecret)
                .build();
    }

    @Bean
    public ObjectStorageSystemService ossProduce() {
        return new MinIoServiceImpl();
    }  
}  