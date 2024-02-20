package org.example.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BaseContext implements IContext{


    /**
     * 转发协议
     */
    protected final String protocol;

    /**
     * 多线程情况下考虑使用volatile
     */
    protected volatile int status = IContext.Running;

    /**
     * Netty上下文对象
     */
    protected final ChannelHandlerContext nettyCtx;

    /**
     * 上下文参数
     */
    protected final Map<String, Object> attributes = new HashMap<>();


    /**
     * 请求过程中发生的异常
     */
    protected Throwable throwable;

    /**
     * 请求是否是长连接
     */
    protected final boolean keepAlive;

    /**
     * 回调函数集合
     */
    protected List<Consumer<IContext>> completedCallBacks;

    /**
     * 请求资源释放释放
     */
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);


    public BaseContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public void running() {
        status = IContext.Running;
    }

    @Override
    public void written() {
        status = IContext.Written;
    }

    @Override
    public void completed() {
        status = IContext.Completed;
    }

    @Override
    public void terminated() {
        status = IContext.Terminated;
    }

    @Override
    public boolean isRunning() {
        return status == IContext.Running;
    }

    @Override
    public boolean isWritten() {
        return status == IContext.Written;
    }

    @Override
    public boolean isCompleted() {
        return status == Completed;
    }

    @Override
    public boolean isTerminated() {
        return status == IContext.Terminated;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public void setResponse(Object response) {

    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public void releaseRequest() {
        this.requestReleased.compareAndSet(false, true);
    }

    @Override
    public void setCompletedCallBack(Consumer<IContext> consumer) {
        if(completedCallBacks == null) {
            completedCallBacks = new ArrayList<>();
        }
        completedCallBacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallBack(Consumer<IContext> consumer) {
        if(completedCallBacks != null) {
            completedCallBacks.forEach(call -> {
                call.accept(this);
            });
        }
    }

    @Override
    public <T> T getAttribute(String key) {
        return (T)attributes.get(key);
    }

    @Override
    public <T> T putAttribute(String key, T value) {
        return (T)attributes.put(key, value);
    }
}
