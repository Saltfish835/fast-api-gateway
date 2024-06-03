package org.example.gateway.core.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.example.gateway.core.Config;
import org.example.gateway.core.LifeCycle;
import org.example.gateway.core.helper.AsyncHttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * 转发客户端请求给下游服务
 */
public class NettyHttpClient implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);

    private final Config config;

    private final EventLoopGroup eventLoopGroupWorker;

    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(Config config, EventLoopGroup eventLoopGroupWorker) {
        this.config = config;
        this.eventLoopGroupWorker = eventLoopGroupWorker;
        init();
    }

    @Override
    public void init() {
        final DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWorker)
                .setConnectTimeout(config.getHttpConnectTimeout())
                .setRequestTimeout(config.getHttpRequestTimeout())
                .setMaxRedirects(config.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT) // 池化byteBuf分配器， 提升性能
                .setCompressionEnforced(true)
                .setMaxConnections(config.getHttpConnectionsPerHost())
                .setMaxConnectionsPerHost(config.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeout());
        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        // 为AsyncHttpHelper类设置真正干活的对象
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
        logger.info("nettyHttpClient start");
    }

    @Override
    public void shutdown() {
        if(asyncHttpClient != null) {
            try{
                asyncHttpClient.close();
            }catch (IOException e) {
                logger.error("NettyHttpClient shutdown error", e);
            }
        }
    }
}
