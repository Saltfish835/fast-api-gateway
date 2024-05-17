package org.example.gateway.core.filter.flowCtl;

import org.example.gateway.common.config.FilterConfig;

public class FlowCtlFilterConfig extends FilterConfig {

    /**
     * 限流类型
     * 基于路径 or 基于服务
     */
    private String type;

    /**
     * 限流对象的值
     */
    private String value;

    /**
     * 限流模式
     * 分布式限流 or 单机限流
     */
    private String model;


    /**
     * 限流配置
     */
    private Config config;

    public FlowCtlFilterConfig() {
    }

    public FlowCtlFilterConfig(String type, String value, String model, Config config) {
        this.type = type;
        this.value = value;
        this.model = model;
        this.config = config;
    }

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

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public static class Config {
        // 时间间隔
        Integer duration;
        // 生成多少个令牌
        Integer permits;

        public Config(Integer duration, Integer permits) {
            this.duration = duration;
            this.permits = permits;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public Integer getPermits() {
            return permits;
        }

        public void setPermits(Integer permits) {
            this.permits = permits;
        }
    }
}
