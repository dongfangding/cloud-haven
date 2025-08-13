package com.snowball.cloud.haven.gateway.interfaces.impl;


import com.snowball.cloud.haven.api.CloudAuthenticationProperties;
import com.snowball.cloud.haven.gateway.exception.GatewayExceptionCode;
import com.snowball.cloud.haven.gateway.interfaces.TokenCustomizeCheckService;
import com.snowball.cloud.haven.gateway.interfaces.UserClaimService;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.util.PreconditionUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2022/05/27 21:56
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DefaultTokenCheckServiceImpl implements TokenCustomizeCheckService {

    private final CloudAuthenticationProperties cloudAuthenticationProperties;
    private final UserClaimService userClaimService;

    /**
     * 业务校验规则
     *
     * @param exchange
     * @param authenticateCheckResult
     * @return
     */
    @Override
    public UserClaim customizeCheck(ServerWebExchange exchange, AuthenticateCheckResult authenticateCheckResult) {
        final HttpHeaders headers = exchange.getRequest().getHeaders();
        final UserClaim tokenUserClaim = authenticateCheckResult.getUserClaim();
        PreconditionUtil.checkArgument(Objects.nonNull(tokenUserClaim), GatewayExceptionCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        PreconditionUtil.checkArgument(!StringUtils.isAnyBlank(tokenUserClaim.getUsername(), tokenUserClaim.getCredit()),
                GatewayExceptionCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        // credit校验
        final String credit = StringUtils.defaultIfBlank(headers.getFirst(cloudAuthenticationProperties.getCreditHeaderName()),
                headers.getFirst("User-Agent"));
        if (Objects.nonNull(tokenUserClaim.getCredit()) && !Objects.equals(tokenUserClaim.getCredit(), credit)) {
            log.error("当前请求credit和token不匹配， 当前: {}, token: {}", credit, tokenUserClaim.getCredit());
            throw new UnauthorizedException(GatewayExceptionCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        }
        // 获取最新用户信息
        UserClaim storeUser = userClaimService.getStoreUserInfo(exchange, tokenUserClaim);
        return storeUser;
    }
}
