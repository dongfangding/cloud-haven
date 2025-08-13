package com.snowball.cloud.haven.core.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/09/18 15:06
 */
@RequestMapping("/register-center")
@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class RegisterCenterController {

    private final AbstractAutoServiceRegistration abstractAutoServiceRegistration;

    public static final String SUCCESS = "SUCCESS";

    @GetMapping("ping")
    public String ping() {
        log.info("注册中心-ping接口被调用");
        return SUCCESS;
    }

    @GetMapping("down")
    public String down() {
        log.info("注册中心-下线接口被调用");
        abstractAutoServiceRegistration.stop();
        log.info("注册中心-下线接口调用完成");
        return SUCCESS;
    }

    @GetMapping("up")
    public String up() {
        log.info("注册中心-上线接口被调用");
        abstractAutoServiceRegistration.start();
        log.info("注册中心-上线接口调用完成");
        return SUCCESS;
    }
}
