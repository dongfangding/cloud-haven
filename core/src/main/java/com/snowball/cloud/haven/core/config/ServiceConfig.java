package com.snowball.cloud.haven.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/23 13:07
 */
@Configuration
public class ServiceConfig {

    /**
     * 负载均衡，服务调用
     *
     * @return
     */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
