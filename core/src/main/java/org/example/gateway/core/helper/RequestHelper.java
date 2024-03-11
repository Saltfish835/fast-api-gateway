package org.example.gateway.core.helper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.*;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.common.constants.GatewayConst;
import org.example.gateway.common.constants.GatewayProtocol;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.request.GatewayRequest;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class RequestHelper {

    public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {

        //	构建请求对象GatewayRequest
        GatewayRequest gateWayRequest = doRequest(request, ctx);

        //	根据请求对象里的uniqueId，从注册中心获取资源服务信息(也就是服务定义信息)
        ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(gateWayRequest.getUniqueId());

        //	根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
        ServiceInvoker serviceInvoker = new HttpServiceInvoker();
        serviceInvoker.setInvokerPath(gateWayRequest.getPath());
        serviceInvoker.setTimeout(500);

        // 根据请求对象获取规则
        final Rule rule = getRule(gateWayRequest, serviceDefinition.getServiceId());

        //	构建我们GatewayContext对象
        GatewayContext gatewayContext = new GatewayContext(
                serviceDefinition.getProtocol(),
                ctx,
                HttpUtil.isKeepAlive(request),
                gateWayRequest,
                rule, 0);


        //后续服务发现做完，这里都要改成动态的
        //gatewayContext.getRequest().setModifyHost("127.0.0.1:8080"); // 在负载均衡算法中实现

        return gatewayContext;
    }

    /**
     *构建Request请求对象
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullHttpRequest.headers();
        //	从header头获取必须要传入的关键属性 uniqueId
        String uniqueId = headers.get(GatewayConst.UNIQUE_ID);
        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);
        GatewayRequest gatewayRequest = new GatewayRequest(uniqueId,charset,clientIp,host,uri,method,contentType,headers,fullHttpRequest);
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
     * 获取rule对象
     * @param gatewayRequest
     * @param serviceId
     * @return
     */
    private static Rule getRule(GatewayRequest gatewayRequest, String serviceId) {
        String key = serviceId + "." + gatewayRequest.getPath();
        Rule rule = DynamicConfigManager.getInstance().getRuleByPath(key);
        if(rule != null) {
            return rule;
        }
        rule = DynamicConfigManager.getInstance().getRuleByServiceId(serviceId).stream().filter(rl -> {
            return gatewayRequest.getPath().startsWith(rl.getPrefix());
        }).findAny().orElseThrow(() -> {
            return new ResponseException(ResponseCode.PATH_NO_MATCHED);
        });
        return rule;
    }

}
