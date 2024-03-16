package org.example.gateway.core.netty.processor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.BaseException;
import org.example.gateway.common.exception.ConnectException;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.filter.FilterFactory;
import org.example.gateway.core.filter.GatewayFilterChain;
import org.example.gateway.core.filter.GatewayFilterChainFactory;
import org.example.gateway.core.helper.AsyncHttpHelper;
import org.example.gateway.core.helper.RequestHelper;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * 处理客户端请求
 */
public class NettyCoreProcessor implements NettyProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NettyCoreProcessor.class);

    private FilterFactory filterFactory = GatewayFilterChainFactory.getInstance();

    /**
     * 处理客户端请求的主流程
     * @param requestWrapper
     */
    @Override
    public void process(HttpRequestWrapper requestWrapper) {
        final FullHttpRequest request = requestWrapper.getRequest();
        final ChannelHandlerContext context = requestWrapper.getContext();

        try {
            // 将netty request对象转换成网关内部上下文对象
            final GatewayContext gatewayContext = RequestHelper.doContext(request, context);
            // 执行过滤器逻辑
            final GatewayFilterChain gatewayFilterChain = filterFactory.buildFilterChain(gatewayContext);
            // 执行过滤器链
            gatewayFilterChain.doFilter(gatewayContext);
        }catch (BaseException e) {
            logger.error("process error {} {}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(context, request, httpResponse);
        } catch (Throwable t) {
            logger.error("process unknown error", t);
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(context, request, httpResponse);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }


    /**
     * 写出响应并释放资源
     * @param context
     * @param request
     * @param response
     */
    private void doWriteAndRelease(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse response) {
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); // 响应请求后关闭channel
        ReferenceCountUtil.release(request); // 释放资源
    }

}
