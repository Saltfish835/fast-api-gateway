package org.example.gateway.common.config;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Rule implements Comparable<Rule>, Serializable {


    /**
     * 唯一id
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则对应的协议
     */
    private String protocol;

    /**
     * 后端服务ID
     */
    private String serviceId;

    /**
     * 请求前缀
     */
    private String prefix;

    /**
     * 路径集合
     */
    private List<String> paths;

    /**
     * 规则优先级
     */
    private Integer order;

    /**
     * 过滤器配置
     */
    private Set<FilterConfig> filterConfigs = new HashSet<>();

    /**
     * 重试次数
     */
    private RetryConfig retryConfig = new RetryConfig();

    /**
     * 限流配置
     */
    private Set<FlowCtlConfig> flowCtlConfigs = new HashSet<>();

    /**
     * 熔断配置
     */
    private Set<HystrixConfig> hystrixConfigs = new HashSet<>();


    public Rule() {
        super();
    }

    public Rule(String id, String name, String protocol, String serviceId, String prefix, List<String> paths, Integer order, Set<FilterConfig> filterConfigs) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.serviceId = serviceId;
        this.prefix = prefix;
        this.paths = paths;
        this.order = order;
        this.filterConfigs = filterConfigs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Set<FilterConfig> getFilterConfigs() {
        return filterConfigs;
    }

    public void setFilterConfigs(Set<FilterConfig> filterConfigs) {
        this.filterConfigs = filterConfigs;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    public Set<FlowCtlConfig> getFlowCtlConfigs() {
        return flowCtlConfigs;
    }

    public void setFlowCtlConfigs(Set<FlowCtlConfig> flowCtlConfigs) {
        this.flowCtlConfigs = flowCtlConfigs;
    }

    public Set<HystrixConfig> getHystrixConfigs() {
        return hystrixConfigs;
    }

    public void setHystrixConfigs(Set<HystrixConfig> hystrixConfigs) {
        this.hystrixConfigs = hystrixConfigs;
    }

    @Override
    public int compareTo(Rule o) {
        final int compare = Integer.compare(getOrder(), o.getOrder());
        // 两者规则优先级相同
        if(compare == 0) {
            // 再比较id
            return getId().compareTo(o.getId());
        }
        return compare;
    }


    /**
     * 添加配置
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }


    /**
     * 根据id返回配置
     * @param id
     * @return
     */
    public FilterConfig getFilterConfig(String id) {
        for(FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }


    /**
     * 判断id对应的规则是否存在
     * @param id
     * @return
     */
    public boolean hashId(String id) {
        for(FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
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
     * 过滤器配置信息
     */
    public static class FilterConfig {

        /**
         * 规则配置id
         */
        private String id;

        /**
         * 配置信息
         */
        private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilterConfig that = (FilterConfig) o;
            return id.equals(that.id);
        }

        /**
         * 重写equals方法的同时一般也需要重写hashCode方法
         * 因为在使用散列数据结构，例如set、map，希望具有相同内容的对象具有相等的哈希码
         * @return
         */
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }



    /**
     * 重试配置信息
     */
    public static class RetryConfig{

        /**
         * 重试次数
         */
        private int times;

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }
    }


    /**
     * 限流配置信息
     */
    public static class FlowCtlConfig {

        /**
         * 限流类型
         */
        private String type;

        /**
         * 限流对象的值
         */
        private String value;

        /**
         * 限流模式，单机or分布式
         */
        private String model;

        /**
         * 限流规则
         */
        private String config;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }
    }


    /**
     * 熔断配置信息
     */
    public static class HystrixConfig {
        private String path;
        private int timeoutInMilliseconds;
        private int threadCoreSize;
        private String fallbackResponse;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getTimeoutInMilliseconds() {
            return timeoutInMilliseconds;
        }

        public void setTimeoutInMilliseconds(int timeoutInMilliseconds) {
            this.timeoutInMilliseconds = timeoutInMilliseconds;
        }

        public int getThreadCoreSize() {
            return threadCoreSize;
        }

        public void setThreadCoreSize(int threadCoreSize) {
            this.threadCoreSize = threadCoreSize;
        }

        public String getFallbackResponse() {
            return fallbackResponse;
        }

        public void setFallbackResponse(String fallbackResponse) {
            this.fallbackResponse = fallbackResponse;
        }
    }

}
