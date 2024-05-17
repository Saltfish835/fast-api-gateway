package org.example.gateway.common.config;

public abstract class FilterConfig {

    /**
     * 过滤器唯一id
     */
    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
