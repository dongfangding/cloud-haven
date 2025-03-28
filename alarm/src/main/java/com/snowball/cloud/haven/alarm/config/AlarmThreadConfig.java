package com.snowball.cloud.haven.alarm.config;

import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/06 10:40
 */
@Configuration
public class AlarmThreadConfig {

    /**
     * 异步处理异常告警线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor globalExceptionExecutor() {
        return ThreadBuilderHelper.buildThreadExecutor("global-exception-alarm-executor", 600, 1000, true);
    }
}
