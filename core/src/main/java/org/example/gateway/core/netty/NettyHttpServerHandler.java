package org.example.gateway.core.netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.netty.processor.NettyProcessor;

/**
 * 自定义入站处理器
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }


    /**
     * 处理SocketChannel的读事件
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        final HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
        httpRequestWrapper.setRequest(request);
        httpRequestWrapper.setContext(ctx);

        // 将请求的处理委托出去
        nettyProcessor.process(httpRequestWrapper);
    }


    /**
     * 处理异常事件
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
