package org.example.gateway.core.filter.router.executor;

import org.example.gateway.common.config.FilterConfig;

public class HystrixFilterConfig extends FilterConfig {

    /**
     * 核心线程数
     */
    private Integer threadCoreSize;

    /**
     * 熔断时的响应
     */
    private String fallbackResponse;

    /**
     * 超时时间
     */
    private Integer timeoutInMilliseconds;

    public Integer getThreadCoreSize() {
        return threadCoreSize;
    }

    public void setThreadCoreSize(Integer threadCoreSize) {
        this.threadCoreSize = threadCoreSize;
    }

    public String getFallbackResponse() {
        return fallbackResponse;
    }

    public void setFallbackResponse(String fallbackResponse) {
        this.fallbackResponse = fallbackResponse;
    }

    public Integer getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public void setTimeoutInMilliseconds(Integer timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }
}
