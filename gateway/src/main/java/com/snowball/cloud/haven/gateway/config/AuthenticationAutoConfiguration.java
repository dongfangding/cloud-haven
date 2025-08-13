package com.snowball.cloud.haven.gateway.config;


import com.snowball.cloud.haven.api.CloudAuthenticationProperties;
import com.snowball.cloud.haven.gateway.interfaces.TokenCustomizeCheckService;
import com.snowball.cloud.haven.gateway.interfaces.UserClaimService;
import com.snowball.cloud.haven.gateway.interfaces.impl.DefaultTokenCheckServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块的自动配置类类
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.snowball.cloud.haven.gateway")
public class AuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CloudAuthenticationProperties cloudAuthenticationProperties() {
        return new CloudAuthenticationProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCustomizeCheckService defaultTokenCheckServiceImpl(
            CloudAuthenticationProperties cloudAuthenticationProperties, UserClaimService userClaimService) {
        return new DefaultTokenCheckServiceImpl(cloudAuthenticationProperties, userClaimService);
    }
}
