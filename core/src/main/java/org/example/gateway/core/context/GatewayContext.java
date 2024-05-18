package org.example.gateway.core.context;

import io.micrometer.core.instrument.Timer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.utils.AssertUtil;
import org.example.gateway.core.request.GatewayRequest;
import org.example.gateway.core.response.GatewayResponse;

/**
 * 网关上下文
 */
public class GatewayContext extends BaseContext{

    public GatewayRequest request;

    public GatewayResponse response;

    public Rule rule;

    private int currentRetryTimes = 0;

    private boolean gray;

    private Timer.Sample timerSample;

    private ServiceDefinition serviceDefinition;

    private ServiceInstance serviceInstance;

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive, GatewayRequest request,
                          Rule rule, ServiceDefinition serviceDefinition) {
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
        this.serviceDefinition = serviceDefinition;
    }

    /**
     * 使用建造者模式构建网关上下文对象
     */
    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;
        private GatewayRequest request;
        private Rule rule;
        private boolean keepAlive;
        private ServiceDefinition serviceDefinition;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setRequest(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(request, "request不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            AssertUtil.notNull(serviceDefinition, "serviceDefinition不能为空");
            return new GatewayContext(protocol, nettyCtx, keepAlive, request, rule, serviceDefinition);
        }
    }

    /**
     * 获取上下文参数，不存在则抛出IllegalArgumentException
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getRequiredAttribute(String key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取上下文参数，不存在则返回默认值
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T getAttributeOrDefault(String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器id获取过滤器信息
     * @param filterId
     * @return
     */
    public FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取上下文唯一id
     * @return
     */
    public String getUniqueId() {
        return rule.getUniqueId();
    }

    /**
     * 释放资源
     */
    public void releaseRequest() {
        if(requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }

    /**
     * 获取原始请求对象
     * @return
     */
    public GatewayRequest getOriginRequest() {
        return request;
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = (GatewayResponse)response;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int getCurrentRetryTimes() {
        return currentRetryTimes;
    }

    public void setCurrentRetryTimes(int currentRetryTimes) {
        this.currentRetryTimes = currentRetryTimes;
    }

    public boolean isGray() {
        return gray;
    }

    public void setGray(boolean gray) {
        this.gray = gray;
    }

    public Timer.Sample getTimerSample() {
        return timerSample;
    }

    public void setTimerSample(Timer.Sample timerSample) {
        this.timerSample = timerSample;
    }

    public ServiceDefinition getServiceDefinition() {
        return serviceDefinition;
    }

    public void setServiceDefinition(ServiceDefinition serviceDefinition) {
        this.serviceDefinition = serviceDefinition;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
