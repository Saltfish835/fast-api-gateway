package org.example.gateway.core.filter.loadbalance;

import org.example.gateway.common.config.FilterConfig;

public class LoadBalanceFilterConfig extends FilterConfig {

    /**
     * 指定负载均衡算法
     */
    public String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
