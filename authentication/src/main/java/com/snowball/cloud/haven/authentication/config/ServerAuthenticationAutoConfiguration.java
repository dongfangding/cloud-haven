package com.snowball.cloud.haven.authentication.config;


import com.snowball.cloud.haven.authentication.filter.ServerAuthenticateTokenFilter;
import com.snowball.cloud.haven.api.CloudAuthenticationProperties;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证模块的自动配置类类
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.snowball.cloud.haven.authentication")
public class ServerAuthenticationAutoConfiguration implements WebMvcConfigurer {

    @Autowired(required = false)
    private ServerAuthenticateTokenFilter authenticateTokenFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (Objects.nonNull(authenticateTokenFilter)) {
            registry.addInterceptor(authenticateTokenFilter).addPathPatterns("/**");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudAuthenticationProperties cloudAuthenticationProperties() {
        return new CloudAuthenticationProperties();
    }
}
