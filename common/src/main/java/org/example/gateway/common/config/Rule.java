package org.example.gateway.common.config;

import java.io.Serializable;
import java.util.HashSet;
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
     * 规则优先级
     */
    private Integer order;

    /**
     * 过滤器配置
     */
    private Set<FilterConfig> filterConfigs = new HashSet<>();


    public Rule() {
        super();
    }

    public Rule(String id, String name, String protocol, Integer order, Set<FilterConfig> filterConfigs) {
        super();
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.order = order;
        this.filterConfigs = filterConfigs;
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    public Integer getOrder() {
        return order;
    }

    public Set<FilterConfig> getFilterConfigs() {
        return filterConfigs;
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
}
