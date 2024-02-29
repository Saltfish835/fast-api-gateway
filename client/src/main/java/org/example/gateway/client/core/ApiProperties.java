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

    @Override
    public String toString() {
        return "ApiProperties{" +
                "registerAddress='" + registerAddress + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}
