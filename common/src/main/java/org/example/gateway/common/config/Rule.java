package org.example.gateway.common.config;

import org.example.gateway.common.constants.BasicConst;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Rule implements Serializable {


    /**
     * 路由规则id
     */
    private String id;

    /**
     * 下游服务id
     */
    private String serviceId;

    /**
     * 下游服务版本
     */
    private String version;

    /**
     * 下游服务协议
     */
    private String protocol;

    /**
     * 调用下游服务失败后的重试次数
     * 可选
     */
    private Integer retry;

    /**
     * 熔断器配置
     * 可选
     */
    private Breaker breaker;


    /**
     * 断言列表
     * 必须满足全部断言才能命中此规则
     */
    private Set<PredicateConfig> predicateConfigs = new HashSet<>();

    /**
     * 过滤器列表
     * 满足断言的请求会依次经历所有过滤器
     */
    private Set<FilterConfig> filterConfigs = new HashSet<>();


    public Rule() {
        super();
    }


    public Rule(String id, String serviceId, String version, String protocol, Integer retry, Breaker breaker, Set<PredicateConfig> predicateConfigs, Set<FilterConfig> filterConfigs) {
        this.id = id;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.retry = retry;
        this.breaker = breaker;
        this.predicateConfigs = predicateConfigs;
        this.filterConfigs = filterConfigs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Set<PredicateConfig> getPredicateConfigs() {
        return predicateConfigs;
    }

    public void setPredicateConfigs(Set<PredicateConfig> predicateConfigs) {
        this.predicateConfigs = predicateConfigs;
    }

    public Set<FilterConfig> getFilterConfigs() {
        return filterConfigs;
    }

    public void setFilterConfigs(Set<FilterConfig> filterConfigs) {
        this.filterConfigs = filterConfigs;
    }

    public Breaker getBreaker() {
        return breaker;
    }

    public void setBreaker(Breaker breaker) {
        this.breaker = breaker;
    }

    public String getUniqueId() {
        return this.serviceId + BasicConst.COLON_SEPARATOR + this.version;
    }

    public FilterConfig getFilterConfig(String filterId) {
        for(FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equals(filterId)) {
                return filterConfig;
            }
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if (o == null || getClass() !=o.getClass()) {
            return  false;
        }
        Rule that = (Rule) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }



    /**
     * 熔断器配置
     */
    public static class Breaker {
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

        public Breaker() {
        }

        public Breaker(Integer threadCoreSize, String fallbackResponse, Integer timeoutInMilliseconds) {
            this.threadCoreSize = threadCoreSize;
            this.fallbackResponse = fallbackResponse;
            this.timeoutInMilliseconds = timeoutInMilliseconds;
        }

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

}
