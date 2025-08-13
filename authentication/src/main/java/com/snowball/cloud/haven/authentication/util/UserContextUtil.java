package com.snowball.cloud.haven.authentication.util;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

;

/**
 * <p>获取当前用户信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 22:33
 */
@Slf4j
public class UserContextUtil {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    public static String getClientIp() {
        return getRequestContext().getClientIp();
    }

    public static String getAppLanguage() {
        return getRequestContext().getAppLanguage();
    }

    public static String getClientIpFromGateway() {
        return getRequestContext().getClientIpFromGateway();
    }

    public static String getRequestUri() {
        return getRequestContext().getRequestUri();
    }

    public static String getSign() {
        return getRequestContext().getSign();
    }

    public static Integer getVersionCode() {
        return getRequestContext().getVersionCode();
    }

    public static String getImei() {
        return getRequestContext().getImei();
    }

    public static Long getNonce() {
        return getRequestContext().getNonce();
    }

    public static OsEnum getOs() {
        return getRequestContext().getOs();
    }

    public static String getOsVersion() {
        return getRequestContext().getOsVersion();
    }


    public static String getUserId() {
        return getRequestContext().getUserIdFromGateway();
    }

    public static String getDeviceMode() {
        return getRequestContext().getDeviceMode();
    }

    public static Long getLongUserId() {
        try {
            return Long.parseLong(getUserId());
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return 0L;
        }
    }


    /**
     * 设置请求上下文
     *
     * @param requestHeader
     */
    public static void setRequestContext(RequestContext requestHeader) {
        REQUEST_CONTEXT.set(requestHeader);
    }

    /**
     * 获取请求上下文
     *
     * @return
     */
    public static RequestContext getRequestContext() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * 移除请求上下文
     */
    public static void removeRequestContext() {
        REQUEST_CONTEXT.remove();;
    }


    /**
     * 获取当前app语言，默认英文
     *
     * @return
     */
    public static String getLanguageOrDefault() {
        String defaultLanguage = "en";
        String language = StringUtils.defaultIfBlank(getRequestContext().getAppLanguage(), defaultLanguage);
        language = Objects.equals("zh-hans", language) ? "zh-hant" : language;
        return language;
    }

    public static Locale getLocale() {
        return new Locale(getLanguageOrDefault());
    }
}
