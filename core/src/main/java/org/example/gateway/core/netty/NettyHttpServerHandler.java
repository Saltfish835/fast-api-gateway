package org.example.gateway.core.netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.netty.processor.NettyProcessor;

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        final HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
        httpRequestWrapper.setRequest(request);
        httpRequestWrapper.setContext(ctx);

        // 将请求的处理委托出去
        nettyProcessor.process(httpRequestWrapper);
    }
}
