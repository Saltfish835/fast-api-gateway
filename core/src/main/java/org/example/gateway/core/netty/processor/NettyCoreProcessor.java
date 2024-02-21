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
            // 将请求路由给下游服务
            route(gatewayContext);
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

    /**
     * 将请求路由给下游服务
     * @param gatewayContext
     */
    private void route(GatewayContext gatewayContext) {
        // 使用网关request对象构建AsyncHttp request对象
        final Request request = gatewayContext.getRequest().build();
        // 发送AsyncHttp请求
        final CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        // 单异步还是双异步
        final boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if(whenComplete) {
            // 单异步
            future.whenComplete(((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            }));
        }else {
            // 双异步
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            }));
        }
    }


    /**
     * 处理响应
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext) {
        // 释放请求资源
        gatewayContext.releaseRequest();
        try {
            if(Objects.nonNull(throwable)) {
                // 处理异常
                String url = request.getUrl();
                if(throwable instanceof TimeoutException) {
                    logger.warn("complete time out {}", url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                }else {
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }else {
                // 使用AsyncHttp response对象构建网关response对象
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        }catch (Throwable t) {
            // 捕获其它异常
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            logger.error("complete error", t);
        }finally {
            // 不管成功失败都需要向客户端响应请求
            gatewayContext.written(); // 记录请求阶段
            ResponseHelper.writeResponse(gatewayContext); // 响应
        }
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
