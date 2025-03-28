package com.snowball.cloud.haven.alarm.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Lark配置</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/04 14:56
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "customizer.alarm.lark")
public class LarkProperties {

    /**
     * 业务告警-资源告警机器人
     */
   private Properties bizResource;

    /**
     * 业务告警-代码异常告警机器人
     */
   private Properties codeException;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {

        /**
         * 是否开启机器人
         */
        private boolean enabled = true;

        /**
         * webhook地址全路径
         * Lark webhook固定地址为https://open.larksuite.com/open-apis/bot/v2/hook/，
         * 然后新增机器人之后还会有一个动态的url后缀，如https://open.larksuite.com/open-apis/bot/v2/hook/ccds-deff-wewe-eww-dsss
         */
        private String webhookUrl;

        /**
         * 加签密钥
         */
        private String secret;
    }
}
