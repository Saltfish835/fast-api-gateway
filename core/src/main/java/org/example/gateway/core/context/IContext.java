package org.example.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

public interface IContext {

    /**
     * 上下文生命周期， 运行中状态
     */
    int Running = 1;

    /**
     * 上下文生命周期， 运行过程中发生错误，请求结束需要返回客户端
     */
    int Written = 0;

    /**
     * 上下文生命周期， 写回成功
     */
    int Completed = 1;

    /**
     * 上下文生命周期， 网关请求结束
     */
    int Terminated = 2;


    /**
     * 设置上下文状态为运行中
     */
    void running();

    /**
     * 设置上下文状态为标记写回
     */
    void written();

    /**
     * 设置上下文状态为标记写回成功
     */
    void completed();

    /**
     * 设置上下文状态为请求结束
     */
    void terminated();

    /**
     * 判断网关状态
     * @return
     */
    boolean isRunning();
    boolean isWritten();
    boolean isCompleted();
    boolean isTerminated();


    /**
     * 获取协议
     * @return
     */
    String getProtocol();

    /**
     * 获取请求对象
     * @return
     */
    Object getRequest();

    /**
     * 获取响应对象
     * @return
     */
    Object getResponse();

    /**
     * 设置响应对象
     * @param response
     */
    void setResponse(Object response);

    /**
     * 获取异常对象
     * @return
     */
    Throwable getThrowable();

    /**
     * 设置异常对象
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取Netty上下文对象
     * @return
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 当前请求是否是长连接
     * @return
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源
     * @return
     */
    void releaseRequest();

    /**
     * 设置写回接收回调函数
     * @param consumer
     */
    void setCompletedCallBack(Consumer<IContext> consumer);

    /**
     * 执行接收回调函数
     */
    void invokeCompletedCallBack();

    /**
     * 获取上下文参数
     * @param key
     * @param <T>
     * @return
     */
    <T> T getAttribute(String key);

    /**
     * 设置上下文参数
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T> T putAttribute(String key, T value);
}
