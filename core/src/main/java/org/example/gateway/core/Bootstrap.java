package org.example.gateway.core;

import com.alibaba.fastjson.JSON;
import org.example.gateway.common.config.DynamicConfigManager;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.utils.NetUtils;
import org.example.gateway.common.utils.TimeUtil;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.core.helper.RuleHelper;
import org.example.gateway.register.center.api.RegisterCenter;
import org.example.gateway.register.center.api.RegisterCenterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;

import java.util.Set;

import static org.example.gateway.common.constants.BasicConst.COLON_SEPARATOR;

/**
 * api网关启动类
 */
public class Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {

        // 加载网关配置
        Config config = ConfigLoader.getInstance().load(args);

        // 插件初始化，保存在config对象中
        PluginLoader.getInstance().load(config);

        // 启动容器
        logger.info("gateway is starting...");
        final Container container = new Container(config);
        container.start();

        // 连接配置中心，拉取配置并保留到本地，订阅配置变更
        pullAndSubscribe(config);

        // 连接注册中心，将网关服务注册到注册中心，拉取服务并保留到本地，订阅服务变更
        registerAndSubscribe(config);

        // 服务优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread() {
            // 接收到kill信号后进行处理
            @Override
            public void run() {
                config.getRegisterCenter().deregister(buildGatewayServiceDefinition(config), buildGatewayServiceInstance(config));
                container.shutdown();
                logger.info("gateway has shutdown");
            }
        });
        logger.info("gateway has started");
    }


    /**
     * 连接配置中心，拉取配置并保留到本地，订阅配置变更
     * @param config
     */
    private static void pullAndSubscribe(Config config) {
        // 初始化配置中心客户端
        ConfigCenter configCenter = config.getConfigCenter();
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        // 从配置中心拉取配置
        configCenter.subscribeRulesChange(configJsonStr -> {
            // 将从配置中心拉取到的json字符串类型的配置转换成rule对象，并保留到本地
            DynamicConfigManager.getInstance().putAllRule(RuleHelper.parseRule(configJsonStr));
        });
    }


    /**
     * 连接注册中心，将网关服务注册到注册中心，拉取服务并保留到本地，订阅服务变更
     * @param config
     */
    private static void registerAndSubscribe(Config config) {
        // 加载注册中心插件
        RegisterCenter registerCenter = config.getRegisterCenter();
        // 初始化注册中心客户端
        registerCenter.init(config.getRegistryAddress(), config.getEnv());
        final ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        final ServiceInstance serviceInstance = buildGatewayServiceInstance(config);
        // 向注册中心注册网关服务
        registerCenter.register(serviceDefinition, serviceInstance);
        // 从注册中心拉取服务
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                logger.info("refresh service and instance: {} {}", serviceDefinition.getUniqueId(),
                        JSON.toJSON(serviceInstanceSet));
                final DynamicConfigManager dynamicConfigManager = DynamicConfigManager.getInstance();
                // 将注册中心的服务保存到本地
                dynamicConfigManager.addServiceInstance(serviceDefinition.getUniqueId(), serviceInstanceSet);
                dynamicConfigManager.putServiceDefinition(serviceDefinition.getUniqueId(), serviceDefinition);
            }
        });
    }



    /**
     * 构建ServiceDefinition
     * @param config
     * @return
     */
    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        final ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(new HashMap<>());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }


    /**
     * 构建ServiceInstance
     * @param config
     * @return
     */
    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        final String localIp = NetUtils.getLocalIp();
        final int port = config.getPort();
        final ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + COLON_SEPARATOR + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

}
