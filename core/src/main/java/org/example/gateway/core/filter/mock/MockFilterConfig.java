package org.example.gateway.core.filter.mock;

import org.example.gateway.common.config.FilterConfig;

public class MockFilterConfig extends FilterConfig {

    /**
     * mock返回的信息
     */
    String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
