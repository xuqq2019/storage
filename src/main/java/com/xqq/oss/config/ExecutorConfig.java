package com.xqq.oss.config;

import com.xqq.oss.config.CustomUnblockThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * 构建线程池配置,解决并发处理响应问题
 * @author xuqq
 * @version v1.0
 * @date 2020-10-14
 */
@Configuration
public class ExecutorConfig {
    /**
     * 初始化线程池
     * @author xuqq
     * @date 2020-09-11
     */
    @Bean
    public ExecutorService executorServiceRegistrationBean() {
        CustomUnblockThreadPoolExecutor exec = new CustomUnblockThreadPoolExecutor();
        //1. 初始化
        exec.init();
        return exec.getCustomThreadPoolExecutor();
    }
}
