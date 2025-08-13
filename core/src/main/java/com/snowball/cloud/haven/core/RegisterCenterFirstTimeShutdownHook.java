package com.snowball.cloud.haven.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/09/18 15:18
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
@Order(value = Integer.MIN_VALUE + 10)
@Component
public class RegisterCenterFirstTimeShutdownHook implements ApplicationListener<ContextClosedEvent> {

    private final AbstractAutoServiceRegistration abstractAutoServiceRegistration;

    /**
     * 如果有自动化运维功力的话，这段代码不需要。最好是在脚本里发布服务前先调用服务的注册中心下线接口，睡眠一会，再重启服务。
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("注册中心-服务关闭事件手动调用下线");
        abstractAutoServiceRegistration.stop();
        log.info("注册中心-服务关闭事件手动调用下线完成");
    }
}
