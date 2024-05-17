package org.example.gateway.core.filter.prefixPath;

import org.example.gateway.common.config.FilterConfig;

public class PrefixPathFilterConfig extends FilterConfig {

    /**
     * 指定要添加的前缀
     */
    public String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
