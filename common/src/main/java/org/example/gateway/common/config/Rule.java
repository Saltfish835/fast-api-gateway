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


}
