package com.snowball.cloud.haven.authentication.filter;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.snowball.cloud.haven.authentication.util.UserContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Configuration;

/**
 * <p>基于open-feign添加请求头</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/12/25 19:18
 */
@Configuration
public class OpenFeignAppendHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        final RequestContext requestContext = UserContextUtil.getRequestContext();
        // 如果是异步、甚至是MQ里面调用的话，这里是没有值的。
        template.header(RequestHeaderEnum.OS.getName(), ObjectUtils.defaultIfNull(UserContextUtil.getOs(), OsEnum.UNKNOWN).name());
        template.header(RequestHeaderEnum.IMEI.getName(), UserContextUtil.getImei());
        template.header(RequestHeaderEnum.NONCE.getName(), String.valueOf(ObjectUtils.defaultIfNull(UserContextUtil.getNonce(), 0L)));
        template.header(RequestHeaderEnum.VERSION_CODE.getName(), String.valueOf(ObjectUtils.defaultIfNull(UserContextUtil.getVersionCode(), 0L)));
        template.header(RequestHeaderEnum.VERSION.getName(), requestContext.getVersion());
        template.header(RequestHeaderEnum.APP_LANGUAGE.getName(), UserContextUtil.getAppLanguage());
        template.header(RequestHeaderEnum.SYSTEM_LANGUAGE.getName(), requestContext.getSystemLanguage());
        template.header(RequestHeaderEnum.USE_PROXY.getName(), String.valueOf(ObjectUtils.defaultIfNull(requestContext.getUseProxy(), false)));
        template.header(RequestHeaderEnum.USE_VPN.getName(), String.valueOf(ObjectUtils.defaultIfNull(requestContext.getUseVpn(), false)));
        template.header(RequestHeaderEnum.TIME_ZONE.getName(), requestContext.getTimeZone());
        template.header(RequestHeaderEnum.OS_VERSION.getName(), requestContext.getOsVersion());
        template.header(RequestHeaderEnum.DEVICE_MODE.getName(), requestContext.getDeviceMode());
        template.header(RequestHeaderEnum.H5_VERSION.getName(), requestContext.getH5Version());
        template.header(RequestHeaderEnum.IOS_IDFA.getName(), requestContext.getIosIdfa());
        template.header(RequestHeaderEnum.ANDROID_ID.getName(), requestContext.getAndroidId());
        template.header(RequestHeaderEnum.CLIENT_IP.getName(), requestContext.getClientIp());
        template.header(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName(), requestContext.getClientIpFromGateway());
        template.header(RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName(), requestContext.getUserIdFromGateway());
        template.header(RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName(), String.valueOf(ObjectUtils.defaultIfNull(requestContext.getIsGatewayDispatch(), false)));
    }
}
