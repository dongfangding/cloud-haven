package com.snowball.cloud.haven.api.biz.model.properties;

import java.io.Serializable;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * <p>动态业务资源属性</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/03/17 20:22
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "customizer.biz.resource")
public class DynamicBizResourceProperties implements Serializable {

    /**
     * 礼物跑道背景图
     */
    private String giftRunwayBackground = "";

    /**
     * 触发礼物跑道的最小金额
     */
    private Integer giftRunwayMinAmount = 1500;
}
