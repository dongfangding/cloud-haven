package com.snowball.cloud.haven.gateway.exception;

import com.ddf.boot.common.api.exception.BaseCallbackCode;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/23 21:55
 */
public enum GatewayExceptionCode implements BaseCallbackCode {
    GATEWAY_ERROR("GATEWAY_ERROR", "服务异常"),

    USER_INFO_EXPIRED_OR_NOT_EXIST("USER_INFO_EXPIRED_OR_NOT_EXIST", "用户信息已失效，请重新登录"),

    USER_IN_BLACK("USER_IN_BLACK", "您的账号已被封禁"),

    USER_IS_LOCKED("USER_IS_LOCKED", "您的账号因{0}已被封禁至{1}"),
    USER_IS_LOCKED2("USER_INFO_EXPIRED_OR_NOT_EXIST", "您的账号因{0}已被封禁至{1}"),
    USER_IS_LOCKED_FOREVER("USER_IS_LOCKED_FOREVER", "您的账号因{0}已被永久封禁"),
    USER_IS_LOCKED_FOREVER2("USER_INFO_EXPIRED_OR_NOT_EXIST", "您的账号因{0}已被永久封禁"),
    MOBILE_IN_BLACK("MOBILE_IN_BLACK", "手机号已被禁用~"),
    IMEI_IN_BLACK("IMEI_IN_BLACK", "设备已被禁用~"),
    IP_IN_BLACK("IP_IN_BLACK", "IP已被禁用~"),
    CURRENT_REGION_NOT_SUPPORTED("CURRENT_REGION_NOT_SUPPORTED", "当前区域不支持")


    ;

    private final String code;

    private final String description;

    private final String bizMessage;

    GatewayExceptionCode(String code, String description) {
        this.code = code;
        this.description = description;
        this.bizMessage = description;
    }

    GatewayExceptionCode(String code, String description, String bizMessage) {
        this.code = code;
        this.description = description;
        this.bizMessage = bizMessage;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getBizMessage() {
        return bizMessage;
    }
}
