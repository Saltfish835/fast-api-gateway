package org.example.gateway.core.filter.stripPrefix;

import org.example.gateway.common.config.FilterConfig;

public class StripPrefixFilterConfig extends FilterConfig {

    /**
     * 指定去掉几层前缀
     */
    public Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
