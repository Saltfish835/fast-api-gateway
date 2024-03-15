package org.example.gateway.core;

import org.example.gateway.common.constants.GatewayConst;
import org.example.gateway.core.netty.NettyHttpClient;
import org.example.gateway.core.netty.NettyHttpServer;
import org.example.gateway.core.netty.processor.DisruptorNettyCoreProcessor;
import org.example.gateway.core.netty.processor.NettyProcessor;
import org.example.gateway.core.netty.processor.NettyCoreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网关容器
 * 整合网关所有的服务
 */
public class Container implements LifeCycle{

    private static final Logger logger = LoggerFactory.getLogger(Container.class);

    /**
     * 网关的所有配置
     */
    private final Config config;

    /**
     * 接收客户端请求的服务
     */
    private NettyHttpServer nettyHttpServer;

    /**
     * 转发客户端请求的服务
     */
    private NettyHttpClient nettyHttpClient;

    /**
     * 处理客户端请求的核心逻辑
     */
    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        final NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        if(GatewayConst.BUFFER_TYPE_PARALLEL.equalsIgnoreCase(config.getBufferType())) {
            this.nettyProcessor = new DisruptorNettyCoreProcessor(config, nettyCoreProcessor);
        }else {
            this.nettyProcessor = nettyCoreProcessor;
        }
        nettyProcessor = new NettyCoreProcessor();
        nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventLoopGroupWorker());
    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();
        nettyHttpClient.start();
        logger.info("fast-api-gateway started!");
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutdown();
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
