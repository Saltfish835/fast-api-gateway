package org.example.gateway.client.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("api-gateway")
public class ApiProperties {

    /**
     * 注册中心
     */
    private String registerAddress;

    /**
     * 环境
     */
    private String env = "dev";

    /**
     * 是否是灰度服务
     */
    private boolean gray;

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public boolean isGray() {
        return gray;
    }

    public void setGray(boolean gray) {
        this.gray = gray;
    }

    @Override
    public String toString() {
        return "ApiProperties{" +
                "registerAddress='" + registerAddress + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}
