package org.example.gateway.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.example.gateway.common.utils.RemotingUtil;
import org.example.gateway.core.Config;
import org.example.gateway.core.LifeCycle;
import org.example.gateway.core.netty.processor.NettyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 接收客户端请求
 */
public class NettyHttpServer implements LifeCycle {

    private final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private final Config config;
    private final NettyProcessor nettyProcessor;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroupBoss;
    private EventLoopGroup eventLoopGroupWorker;

    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
        init();
    }

    /**
     * 当前环境是否可以用epoll
     * @return
     */
    public boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if(useEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("netty-worker-nio"));
        }else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("netty-worker-nio"));
        }
    }

    @Override
    public void start() {
        // 配置netty server
        this.serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new HttpServerCodec())  // http编解码
                                .addLast(new HttpObjectAggregator(config.getMaxContentLength())) // 将http报文聚合成FullHttpRequest
                                .addLast(new NettyServerConnectManagerHandler()) // 管理netty连接
                                .addLast(new NettyHttpServerHandler(nettyProcessor)); // 自定义核心处理逻辑
                    }
                });
        // 启动netty server
        try{
            this.serverBootstrap.bind().sync();
            logger.info("netty server startup on port {}", this.config.getPort());
        }catch (Exception e) {
            logger.error("netty server start error.", e);
            throw new RuntimeException();
        }
    }

    @Override
    public void shutdown() {
        if(eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if(eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();;
        }
    }

    public Config getConfig() {
        return config;
    }

    public NettyProcessor getNettyProcessor() {
        return nettyProcessor;
    }

    public ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    public EventLoopGroup getEventLoopGroupBoss() {
        return eventLoopGroupBoss;
    }

    public EventLoopGroup getEventLoopGroupWorker() {
        return eventLoopGroupWorker;
    }
}
