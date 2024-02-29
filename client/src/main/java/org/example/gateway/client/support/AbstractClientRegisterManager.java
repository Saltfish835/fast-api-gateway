package org.example.gateway.client.support;

import org.example.gateway.client.core.ApiProperties;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.loader.GatewayServiceLoader;
import org.example.gateway.register.center.api.RegisterCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractClientRegisterManager {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClientRegisterManager.class);

    private ApiProperties apiProperties;

    /**
     * 注册中心客户端
     */
    private RegisterCenter registerCenter;

    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        // 使用SPI机制去拿到具体的注册中心实现
//        final ServiceLoader<RegisterCenter> centerServiceLoader = ServiceLoader.load(RegisterCenter.class);
//        for (RegisterCenter registerCenterTmp : centerServiceLoader) {
//            // 拿到第一个
//            registerCenter = registerCenterTmp;
//            break;
//        }
        registerCenter = GatewayServiceLoader.load(RegisterCenter.class);
        if(registerCenter == null) {
            logger.error("not found RegisterCenter impl");
            throw  new RuntimeException("not found RegisterCenter impl");
        }
        registerCenter.init(apiProperties.getRegisterAddress(), apiProperties.getEnv());
    }

    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        registerCenter.register(serviceDefinition, serviceInstance);
    }

    public ApiProperties getApiProperties() {
        return apiProperties;
    }
}
