package com.snowball.cloud.haven.api.config;

import com.snowball.cloud.haven.api.CloudAuthenticationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/06/04 14:08
 */
@Configuration
@ComponentScan("com.snowball.cloud.haven.api")
@EnableConfigurationProperties(CloudAuthenticationProperties.class)
public class CloudApiAutoConfiguration {

}
