package org.example.gateway.core.netty.processor;

import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.core.Config;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.disruptor.EventListener;
import org.example.gateway.core.disruptor.ParallelQueueHandler;
import org.example.gateway.core.helper.ResponseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Disruptor流程处理类
 */
public class DisruptorNettyCoreProcessor implements NettyProcessor{

    private static final Logger logger = LoggerFactory.getLogger(DisruptorNettyCoreProcessor.class);

    private static final String THREAD_NAME_PREFIX = "gateway-queue-";

    private Config config;

    private NettyCoreProcessor nettyCoreProcessor;

    private ParallelQueueHandler<HttpRequestWrapper> parallelQueueHandler;

    public DisruptorNettyCoreProcessor(Config config, NettyCoreProcessor nettyCoreProcessor) {
        this.config = config;
        this.nettyCoreProcessor = nettyCoreProcessor;
        ParallelQueueHandler.Builder builder = new ParallelQueueHandler.Builder().setBufferSize(config.getBufferSize())
                .setThreads(config.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(config.getWaitStrategy());
        final BatchEventListenerProcessor batchEventListenerProcessor = new BatchEventListenerProcessor();
        builder.setListener(batchEventListenerProcessor);
        this.parallelQueueHandler = builder.build();
    }

    @Override
    public void process(HttpRequestWrapper requestWrapper) {
        this.parallelQueueHandler.add(requestWrapper);
    }



    public class BatchEventListenerProcessor implements EventListener<HttpRequestWrapper> {
        @Override
        public void onEvent(HttpRequestWrapper event) {
            nettyCoreProcessor.process(event);
        }

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

    @Override
    public void start() {
        parallelQueueHandler.start();
    }

    @Override
    public void shutdown() {
        parallelQueueHandler.shutdown();
    }
}
