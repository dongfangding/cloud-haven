package com.snowball.cloud.haven.authentication.filter;


import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.snowball.cloud.haven.authentication.config.AuthenticateConstant;
import com.snowball.cloud.haven.authentication.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截请求处理用户认证信息
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Slf4j
@Component
public class ServerAuthenticateTokenFilter implements HandlerInterceptor {

    /**
     * 前置校验
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 解析请求头
        resolveRequestContext(request);
        MDC.put(AuthenticateConstant.MDC_USER_ID, UserContextUtil.getUserId());
        MDC.put(AuthenticateConstant.MDC_TRACE_ID,
                request.getHeader(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName())
        );
        MDC.put(AuthenticateConstant.MDC_CLIENT_IP, UserContextUtil.getClientIpFromGateway());
        MDC.put(AuthenticateConstant.MDC_IMEI, UserContextUtil.getImei());
        return true;
    }

    /**
     * 执行器结束
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 移除用户信息
        UserContextUtil.removeRequestContext();
        MDC.remove(AuthenticateConstant.MDC_USER_ID);
        MDC.remove(AuthenticateConstant.MDC_TRACE_ID);
        MDC.remove(AuthenticateConstant.MDC_CLIENT_IP);
        MDC.remove(AuthenticateConstant.MDC_IMEI);
    }

    /**
     * 解析请求上下文
     *
     * @param request
     */
    public void resolveRequestContext(HttpServletRequest request) {
        UserContextUtil.setRequestContext(RequestContext
                .builder()
                .sign(request.getHeader(RequestHeaderEnum.SIGN.getName()))
                .os(OsEnum.resolve(request.getHeader(RequestHeaderEnum.OS.getName())))
                .imei(request.getHeader(RequestHeaderEnum.IMEI.getName()))
                .nonce(Long.parseLong(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.NONCE.getName()), "0")))
                .versionCode(Integer.parseInt(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.VERSION_CODE.getName()), "0")))
                .version(request.getHeader(RequestHeaderEnum.VERSION.getName()))
                .appLanguage(request.getHeader(RequestHeaderEnum.APP_LANGUAGE.getName()))
                .systemLanguage(request.getHeader(RequestHeaderEnum.SYSTEM_LANGUAGE.getName()))
                .useProxy(Boolean.parseBoolean(request.getHeader(RequestHeaderEnum.USE_PROXY.getName())))
                .useVpn(Boolean.parseBoolean(request.getHeader(RequestHeaderEnum.USE_VPN.getName())))
                .timeZone(request.getHeader(RequestHeaderEnum.TIME_ZONE.getName()))
                .osVersion(request.getHeader(RequestHeaderEnum.OS_VERSION.getName()))
                .deviceMode(request.getHeader(RequestHeaderEnum.DEVICE_MODE.getName()))
                .h5Version(request.getHeader(RequestHeaderEnum.H5_VERSION.getName()))
                .iosIdfa(request.getHeader(RequestHeaderEnum.IOS_IDFA.getName()))
                .androidId(request.getHeader(RequestHeaderEnum.ANDROID_ID.getName()))
                .requestUri(request.getRequestURI())
                .clientIp(request.getHeader(RequestHeaderEnum.CLIENT_IP.getName()))
                .clientIpFromGateway(request.getHeader(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName()))
                .isGatewayDispatch(Boolean.parseBoolean(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName()),
                                "false"
                        )))
                .userIdFromGateway(request.getHeader(RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName()))
                .isSimulator(Boolean.parseBoolean(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.SIMULATOR.getName()), "false")))
                .build());
    }
}
