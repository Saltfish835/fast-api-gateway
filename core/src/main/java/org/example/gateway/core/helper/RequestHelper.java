package org.example.gateway.core.helper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.*;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.predicates.method.MethodPredicateConfig;
import org.example.gateway.core.predicates.path.PathPredicateConfig;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(RequestHelper.class);


    /**
     * 构建网关上下文对象
     * @param request
     * @param ctx
     * @return
     */
    public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
        //	构建请求对象GatewayRequest
        GatewayRequest gateWayRequest = doRequest(request, ctx);
        // 获取请求对应的路由规则
        final Rule rule = getRule(gateWayRequest);
        if(rule == null) {
            throw new RuntimeException("请求没有对应的路由规则");
        }
        // 根据规则中的serviceId，从注册中心拿到对应服务注册信息
        ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(rule.getUniqueId());
        //	创建网关上下文对象对象
        GatewayContext gatewayContext = new GatewayContext(serviceDefinition.getProtocol(), ctx, HttpUtil.isKeepAlive(request),
                gateWayRequest, rule, serviceDefinition);
        return gatewayContext;
    }

    /**
     *构建网关请求对象
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullHttpRequest.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);
        GatewayRequest gatewayRequest = new GatewayRequest(charset,clientIp,host,uri,method,contentType,headers,fullHttpRequest);
        return gatewayRequest;
    }

    /**
     * 获取客户端ip
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
        String clientIp = null;
        if(StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if(clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }


    /**
     * 获取请求对应的rule
     * @param gatewayRequest
     * @return
     */
    private static Rule getRule(GatewayRequest gatewayRequest) {
        ConcurrentHashMap<String, Rule> ruleMap = DynamicConfigManager.getInstance().getRuleMap();
        for(Rule rule : ruleMap.values()) {
            boolean flag = true;
            // 需要满足所有predicate，才能认为此request满足此rule
            Set<PredicateConfig> predicateConfigs = rule.getPredicateConfigs();
            for(PredicateConfig predicateConfig : predicateConfigs) {
                boolean isMatch = isMatch(gatewayRequest, predicateConfig);
                // 如果有一条predicate不能匹配，说明此request不适合此rule
                if(isMatch == false) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                return rule;
            }
        }
        return null;
    }


    /**
     * 判断请求与predicate是否匹配
     * @param gatewayRequest
     * @param predicateConfig
     * @return
     */
    private static boolean isMatch(GatewayRequest gatewayRequest, PredicateConfig predicateConfig) {
        String id = predicateConfig.getId();
        if("path".equals(id)) { // 基于路径匹配
            String reqPath = gatewayRequest.getPath(); // 当前请求路径
            List<String> paths = ((PathPredicateConfig) predicateConfig).getValues(); // 当前配置路径
            for(String path : paths) {
                // 如果当前用户请求路径是以此配置路径开头，说明匹配上了
                if(reqPath.startsWith(path)) {
                    return true;
                }
            }
            return false;
        }else if("method".equals(id)) { // 基于请求方式匹配
            String reqMethod = gatewayRequest.getHttpMethod().toString();
            List<String> methods = ((MethodPredicateConfig) predicateConfig).getValues();
            for(String method : methods) {
                // 如果当前用户请求方式与配置的请求方式一致，说明匹配上了
                if(reqMethod.equals(method)) {
                    return true;
                }
            }
            return false;
        }else {

        }
        return false;
    }
}
