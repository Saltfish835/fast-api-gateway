package org.example.gateway.core.disruptor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.netty.processor.NettyCoreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件处理器
 */
public class BatchEventListenerProcessor implements EventListener<HttpRequestWrapper> {

    private static final Logger logger = LoggerFactory.getLogger(BatchEventListenerProcessor.class);

    private NettyCoreProcessor nettyCoreProcessor;

    public BatchEventListenerProcessor(NettyCoreProcessor nettyCoreProcessor) {
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    /**
     * 正常处理事件
     * @param event
     */
    @Override
    public void onEvent(HttpRequestWrapper event) {
        // 最终还是调用nettyCoreProcessor来处理
        nettyCoreProcessor.process(event);
    }

    /**
     * 异常处理事件
     * @param ex
     * @param sequence
     * @param event
     */
    @Override
    public void onException(Throwable ex, long sequence, HttpRequestWrapper event) {
        final FullHttpRequest request = event.getRequest();
        final ChannelHandlerContext context = event.getContext();
        try{
            logger.error("BatchEventListenerProcessor onException请求写回失败，request:{},errMsg:{} ",request,ex.getMessage(),ex);
            // 构建响应对象
            final FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            if(!HttpUtil.isKeepAlive(request)) {
                context.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }else {
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                context.writeAndFlush(httpResponse);
            }
        }catch (Exception e) {
            logger.error("BatchEventListenerProcessor onException请求写回失败，request:{},errMsg:{} ",request,e.getMessage(),e);
        }
    }
}