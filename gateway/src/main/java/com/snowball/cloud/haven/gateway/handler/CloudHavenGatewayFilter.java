package com.snowball.cloud.haven.gateway.handler;


import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.authentication.TokenUtil;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.util.GlobalAntMatcher;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.SignatureUtil;
import com.snowball.cloud.haven.api.CloudAuthenticationProperties;
import com.snowball.cloud.haven.gateway.exception.GatewayExceptionCode;
import com.snowball.cloud.haven.gateway.interfaces.TokenCustomizeCheckService;
import com.snowball.cloud.haven.gateway.interfaces.UserClaimService;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>网关全局认证过滤器</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/23 10:21
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CloudHavenGatewayFilter implements GlobalFilter {

    public static final String BEAN_NAME = "PanguGatewayFilter";

    private final CloudAuthenticationProperties cloudAuthenticationProperties;
    private final EnvironmentHelper environmentHelper;
    private final UserClaimService userClaimService;
    private final TokenCustomizeCheckService tokenCustomizeCheckService;
    private final CodecConfigurer codecConfigurer;

    /**
     * 获取客户端请求ip
     *
     * @param request
     * @return
     */
    public static String getClientIp(ServerHttpRequest request) {
        final HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip) && ip.contains(",")) {
            ip = ip.split(",")[0];
        }

        if (isInvalidIp(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }

        if (isInvalidIp(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }

        if (isInvalidIp(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }

        if (isInvalidIp(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }

        if (isInvalidIp(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (isInvalidIp(ip) && request.getRemoteAddress() != null) {
            ip = request
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress();
        }
        return ip;
    }

    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            final ServerHttpRequest request = exchange.getRequest();
            final HttpHeaders headers = request.getHeaders();
            // 通用请求头解析
            final Map<String, String> clientHeaderMap = resolveClientHeaders(request, headers, null);
            // 自定义请求头解析， 处理过程中可以额外添加请求头，最终会被统一添加到请求头中
            final Map<String, String> customizeHeaderMap = new HashMap<>();
            final String uri = request
                    .getURI()
                    .getPath();
            final List<String> openIgnores = cloudAuthenticationProperties.getOpenIgnores();
            if (GlobalAntMatcher.match(openIgnores, uri)) {
                final ServerHttpRequest.Builder mutate = request.mutate();
                clientHeaderMap.forEach(mutate::header);
                return chain.filter(exchange
                        .mutate()
                        .request(mutate.build())
                        .build());
            }
            // 必传请求头校验， 放在开放接口校验之后
            final Map<String, RequestHeaderEnum> requiredClientHeaders = RequestHeaderEnum.getRequiredClientHeaders();
            for (String name : requiredClientHeaders.keySet()) {
                if (StringUtils.isBlank(headers.getFirst(name))) {
                    return responseErrorJson(exchange.getResponse(), BaseErrorCallbackCode.ILLEGAL_REQUEST);
                }
            }
            // 自定义
            final ResponseData<Object> responseData = userClaimService.beforeTokenVerify(
                    exchange, clientHeaderMap, customizeHeaderMap);
            if (!responseData.isSuccess()) {
                return responseCustomizeMsg(exchange.getResponse(), responseData);
            }
            final List<String> whiteUrlList = cloudAuthenticationProperties.getIgnores();
            final String token = headers.getFirst(cloudAuthenticationProperties.getTokenHeaderName());
            UserClaim userClaim = null;
            // 非白名单
            if (!GlobalAntMatcher.match(whiteUrlList, uri)) {
                try {
                    userClaim = checkAndParseAuthInfo(exchange, token);
                } catch (BaseException e) {
                    return responseErrorJson(exchange.getResponse(), e.getCode(), e.getMessage());
                } catch (Exception e) {
                    return responseErrorJson(exchange.getResponse(), "500", "server error~");
                }
                if (Objects.isNull(userClaim)) {
                    return responseErrorJson(
                            exchange.getResponse(), GatewayExceptionCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
                }
            }
            // 添加服务端请求头
            clientHeaderMap.putAll(resolveServerHeaders(exchange, userClaim));
            userClaimService.afterTokenVerifySuccess(exchange, chain, userClaim, clientHeaderMap, customizeHeaderMap);
            // 重放简单校验
            final long nonce = Long.parseLong(
                    Objects.requireNonNull(headers.getFirst(RequestHeaderEnum.NONCE.getName())));
            if (nonce < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)) {
                return responseErrorJson(exchange.getResponse(), BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
            }

            // 签名校验
            MediaType mediaType = request
                    .getHeaders()
                    .getContentType();
            if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)
                    || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) { // 适合 JSON 和 Form 提交的请求
                return resolveBodySignData(exchange, chain, userClaim, clientHeaderMap, customizeHeaderMap);
            }
            return resolveQueryParamsSignData(exchange, chain, userClaim, clientHeaderMap, customizeHeaderMap);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                return responseErrorJson(exchange.getResponse(), ((BusinessException) e).getBaseCallbackCode());
            }
            log.error("网关处理异常", e);
            return responseErrorJson(exchange.getResponse(), GatewayExceptionCode.GATEWAY_ERROR);
        }
    }


    /**
     * 解析验签参数
     * <p>
     * 参考 {@link ModifyRequestBodyGatewayFilterFactory} 实现
     * <p>
     * 差别主要在于使用 modifiedBody 来读取 Request Body 数据
     */
    private Mono<Void> resolveBodySignData(ServerWebExchange exchange, GatewayFilterChain chain, UserClaim userClaim,
            Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // 此处 codecConfigurer.getReaders() 的目的，是解决 spring.codec.max-in-memory-size 不生效
        ServerRequest serverRequest = ServerRequest.create(exchange, codecConfigurer.getReaders());
        AtomicReference<String> requestBody = new AtomicReference<>("");
        Mono<String> modifiedBody = serverRequest
                .bodyToMono(String.class)
                .flatMap(body -> {
                    requestBody.set(body);
                    return Mono.just(body);
                });

        // 创建 BodyInserter 对象
        BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(
                modifiedBody, String.class);
        // 创建 CachedBodyOutputMessage 对象
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange
                .getRequest()
                .getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH); // 移除
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        // 通过 BodyInserter 将 Request Body 写入到 CachedBodyOutputMessage 中
        return bodyInserter
                .insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    // 包装 Request，用于缓存 Request Body
                    ServerHttpRequest decoratedRequest = requestDecorate(exchange, headers, outputMessage);
                    return validSign(
                            exchange, chain, decoratedRequest,
                            StringUtils.isNotBlank(requestBody.get()) ? JsonUtil.toBean(requestBody.get(), Map.class) :
                                    new HashMap<>(), userClaim, headerMap, customizeHeaderMap
                    );
                }));
    }

    private Mono<Void> resolveQueryParamsSignData(ServerWebExchange exchange, GatewayFilterChain chain,
            UserClaim userClaim, Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        final ServerHttpRequest request = exchange.getRequest();
        final Map<String, Object> data = request
                .getQueryParams()
                .toSingleValueMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return validSign(exchange, chain, request, data, userClaim, headerMap, customizeHeaderMap);
    }

    /**
     * 验签
     *
     * @param request
     * @param data
     * @return
     */
    private Mono<Void> validSign(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request,
            Map<String, Object> data, UserClaim userClaim, Map<String, String> headerMap,
            Map<String, String> customizeHeaderMap) {
        final HttpHeaders headers = request.getHeaders();
        // 把请求头中的nonce也加入到加签规则字段中
        data.put(RequestHeaderEnum.NONCE.getName(), headers.getFirst(RequestHeaderEnum.NONCE.getName()));
        final String sign = headers.getFirst(RequestHeaderEnum.SIGN.getName());
        if (!"111111".equals(sign) && cloudAuthenticationProperties.isSignEnabled()
                && !SignatureUtil.verifySelfSignature(data, sign, cloudAuthenticationProperties.getSignSecret())) {
            return responseErrorJson(exchange.getResponse(), BaseErrorCallbackCode.SIGN_ERROR);
        }
        // 分发服务前
        final ResponseData<Object> responseData = userClaimService.beforeDispatch(
                exchange, chain, userClaim, headerMap, customizeHeaderMap);
        if (!responseData.isSuccess()) {
            return responseCustomizeMsg(exchange.getResponse(), responseData);
        }
        headerMap.putAll(customizeHeaderMap);
        final ServerHttpRequest.Builder mutate = request.mutate();
        headerMap.forEach(mutate::header);
        return chain.filter(exchange
                .mutate()
                .request(mutate.build())
                .build());
    }

    /**
     * 解析客户端请求头
     *
     * @param request
     * @param headers
     * @param userClaim
     * @return
     */
    private Map<String, String> resolveClientHeaders(ServerHttpRequest request, HttpHeaders headers,
            UserClaim userClaim) {
        Map<String, String> clientHeaderMap = new HashMap<>();
        // 处理客户端传递的约定好的请求头
        final Map<String, RequestHeaderEnum> clientHeaders = RequestHeaderEnum.getAllClientHeaders();
        clientHeaders.forEach((name, obj) -> {
            clientHeaderMap.put(
                    name, Optional
                            .ofNullable(headers.getFirst(name))
                            .orElse(obj.getDefaultValue())
            );
        });
        return clientHeaderMap;
    }

    /**
     * 解析服务端内部请求头
     *
     * @param exchange
     * @param userClaim
     * @return
     */
    private Map<String, String> resolveServerHeaders(ServerWebExchange exchange, UserClaim userClaim) {
        Map<String, String> serverHeaderMap = new HashMap<>();
        final ServerHttpRequest request = exchange.getRequest();
        final HttpHeaders headers = request.getHeaders();
        // 处理服务端内部的请求头
        serverHeaderMap.put(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName(), getClientIp(request));
        serverHeaderMap.put(
                RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName(),
                Objects.nonNull(userClaim) ? userClaim.getUserId() : headers.getFirst(RequestHeaderEnum.IMEI.getName())
        );
        serverHeaderMap.put(
                RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName(),
                RequestHeaderEnum.IS_GATEWAY_DISPATCH.getDefaultValue()
        );
        serverHeaderMap.put(
                RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName(), generateTraceId(
                        Objects.nonNull(userClaim) ? userClaim.getUserId() : headers.getFirst(RequestHeaderEnum.IMEI.getName()))
        );
        serverHeaderMap.put(
                RequestHeaderEnum.IS_CONSOLE_WHITELIST_IMEI.getName(),
                RequestHeaderEnum.IS_CONSOLE_WHITELIST_IMEI.getDefaultValue()
        );
        return serverHeaderMap;
    }

    /**
     * 返回错误json信息
     *
     * @param response
     * @param bizCode
     * @return
     */
    private Mono<Void> responseErrorJson(ServerHttpResponse response, BaseCallbackCode bizCode) {
        response
                .getHeaders()
                .add("Content-Type", "application/json;charset=UTF-8");
        String result = JsonUtil.toJson(ResponseData.failure(bizCode));
        DataBuffer buffer = response
                .bufferFactory()
                .wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    /**
     * 返回错误json信息
     *
     * @param response
     * @return
     */
    private Mono<Void> responseErrorJson(ServerHttpResponse response, String code, String message) {
        response
                .getHeaders()
                .add("Content-Type", "application/json;charset=UTF-8");
        String result = JsonUtil.toJson(ResponseData.failure(code, "Failed, please contact customer service", message));
        DataBuffer buffer = response
                .bufferFactory()
                .wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    /**
     * 返回自定义文本
     *
     * @param response
     * @param data
     * @return
     */
    public static Mono<Void> responseCustomizeMsg(ServerHttpResponse response, ResponseData<Object> data) {
        response
                .getHeaders()
                .add("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = response
                .bufferFactory()
                .wrap(JsonUtil
                        .toJson(data)
                        .getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    /**
     * 校验并转换用户信息
     *
     * @param exchange
     * @param tokenHeader
     * @return
     */
    private UserClaim checkAndParseAuthInfo(ServerWebExchange exchange, String tokenHeader) {
        final ServerHttpRequest request = exchange.getRequest();
        UserClaim tokenUserClaim;
        if (StringUtils.isBlank(tokenHeader)) {
            throw new UnauthorizedException(BaseErrorCallbackCode.ILLEGAL_TOKEN);
        }
        String tokenPrefix = cloudAuthenticationProperties.getTokenPrefix();
        String token = tokenHeader;
        if (StringUtils.isNotBlank(cloudAuthenticationProperties.getTokenPrefix())) {
            token = tokenHeader.split(tokenPrefix)[1];
        }
        UserClaim storeUser;
        if (cloudAuthenticationProperties.isMock() && !environmentHelper.isProdProfile() && CollUtil.isNotEmpty(
                cloudAuthenticationProperties.getMockUserIdList()) && cloudAuthenticationProperties
                .getMockUserIdList()
                .contains(token)) {
            tokenUserClaim = UserClaim.mockUser(token);
            storeUser = userClaimService.getStoreUserInfo(exchange, tokenUserClaim);
        } else {
            AuthenticateCheckResult authenticateCheckResult = TokenUtil.checkToken(token);
            tokenUserClaim = authenticateCheckResult.getUserClaim();
            // 额外业务token校验规则
            storeUser = tokenCustomizeCheckService.customizeCheck(exchange, authenticateCheckResult);
        }
        return storeUser;
    }

    /**
     * 生成traceId
     *
     * @param userId
     * @return
     */
    private String generateTraceId(String userId) {
        return String.join("-", userId, IdsUtil.getNextStrId());
    }

    // ========== 参考 ModifyRequestBodyGatewayFilterFactory 中的方法 ==========

    /**
     * 请求装饰器，支持重新计算 headers、body 缓存
     *
     * @param exchange      请求
     * @param headers       请求头
     * @param outputMessage body 缓存
     * @return 请求装饰器
     */
    private ServerHttpRequestDecorator requestDecorate(ServerWebExchange exchange, HttpHeaders headers,
            CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {

            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    // TODO: this causes a 'HTTP/1.1 411 Length Required' // on
                    // httpbin.org
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

}
