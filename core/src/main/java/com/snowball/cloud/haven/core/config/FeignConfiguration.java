package com.snowball.cloud.haven.core.config;

import feign.Request;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FeignConfiguration {

    /**
     *
     * feign的默认超时时间只有1000ms,这里向容器中注入一个默认的超时时间， 客户端也可以使用配置      *    的形式来覆盖这个默认的
     */
    @Bean
    @Primary
    public Request.Options options() {
        return new Request.Options(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true);
    }

    /**
     *
     * feign的接口可以接受一个入参对象(Request.Options),这样就可以自定义每个接口的超时时间了，这里预定义几个参数
     * 参考 https://github.com/OpenFeign/feign/pull/970
     *
     * @return
     */
    @Bean
    public Request.Options oneSecondsOptions() {
        return new Request.Options(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS, true);
    }

    /**
       *
       * feign的接口可以接受一个入参对象(Request.Options),这样就可以自定义每个接口的超时时间了，这里预定义几个参数
       * 参考 https://github.com/OpenFeign/feign/pull/970
       *
       * @return
       */
    @Bean
    public Request.Options fiveSecondsOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }

    /**
       *
       * feign的接口可以接受一个入参对象(Request.Options),这样就可以自定义每个接口的超时时间了，这里预定义几个参数
       * 参考 https://github.com/OpenFeign/feign/pull/970
       *
       * @return
       */
    @Bean
    public Request.Options thirtySecondsOptions() {
        return new Request.Options(30, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true);
    }
}
