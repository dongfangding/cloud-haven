package com.snowball.cloud.haven.gateway.interfaces;


import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import org.springframework.web.server.ServerWebExchange;

/**
 * <p>在通用的校验规则上可以实现该接口实现自己的校验规则</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2022/05/25 09:57
 */
public interface TokenCustomizeCheckService {


    /**
     * 自定义校验规则
     *
     * @param request
     * @param authenticateCheckResult
     * @return 返回最新的用户信息
     */
    UserClaim customizeCheck(ServerWebExchange request, AuthenticateCheckResult authenticateCheckResult);
}
