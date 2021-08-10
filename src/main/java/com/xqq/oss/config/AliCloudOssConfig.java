package com.xqq.oss.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.xqq.oss.condition.AliCloudCondition;
import com.xqq.oss.service.ObjectStorageSystemService;
import com.xqq.oss.service.impl.AliCloudServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(AliCloudCondition.class)
public class AliCloudOssConfig {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.access.key.id}")
    private String accessKeyId;
    @Value("${oss.access.key.secret}")
    private String accessKeySecret;


    @Bean
    public OSS objectStorageServiceClient(){
        System.out.println("start alibaba cloud OSS ["+endpoint+"]"+accessKeyId+"]["+accessKeySecret+"]");
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setConnectionTimeout(1000);
        conf.setSocketTimeout(1000);
        conf.setConnectionRequestTimeout(1000);
        conf.setRequestTimeout(1000);
        // 创建OSSClient实例。
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret,conf);
    }

    @Bean
    public ObjectStorageSystemService ossProduce() {
        return new AliCloudServiceImpl();
    }

}
