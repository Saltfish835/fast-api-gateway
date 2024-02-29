package org.example.gateway.core;

import com.alibaba.fastjson.JSON;
import org.example.gateway.common.config.DynamicConfigManager;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.utils.NetUtils;
import org.example.gateway.common.utils.TimeUtil;
import org.example.gateway.common.loader.GatewayServiceLoader;
import org.example.gateway.config.center.api.ConfigCenter;
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
        final Config config = ConfigLoader.getInstance().load(args);

        // 插件初始化

        // 连接配置中心，监听配置的新增、修改、删除
        ConfigCenter configCenter = null;
//        final ServiceLoader<ConfigCenter> configCenterServiceLoader = ServiceLoader.load(ConfigCenter.class);
//        for(ConfigCenter configCenterTmp : configCenterServiceLoader) {
//            configCenter = configCenterTmp;
//            break;
//        }
        configCenter = (ConfigCenter)GatewayServiceLoader.load(ConfigCenter.class);
        if(configCenter == null) {
            logger.error("not found ConfigCenter impl");
            throw new RuntimeException("not found ConfigCenter impl");
        }
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRulesChange(rules -> {
            DynamicConfigManager.getInstance().putAllRule(rules);
        });

        // 启动容器
        final Container container = new Container(config);
        container.start();

        // 连接注册中心，将注册中心的实例加载到本地
        RegisterCenter registerCenter = registerAndSubscribe(config);

        // 服务优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread() {
            // 接收到kill信号后进行处理
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config), buildGatewayServiceInstance(config));
                container.shutdown();
            }
        });

    }


    private static RegisterCenter registerAndSubscribe(Config config) {
        RegisterCenter registerCenter = null;
//        final ServiceLoader<RegisterCenter> registerCenterServiceLoader = ServiceLoader.load(RegisterCenter.class);
//        for(RegisterCenter registerCenterTmp : registerCenterServiceLoader) {
//            registerCenter = registerCenterTmp;
//            break;
//        }
        registerCenter = GatewayServiceLoader.load(RegisterCenter.class);
        if(registerCenter == null) {
            logger.error("not found RegisterCenter impl");
            throw  new RuntimeException("not found RegisterCenter impl");
        }
        registerCenter.init(config.getRegistryAddress(), config.getEnv());
        final ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        final ServiceInstance serviceInstance = buildGatewayServiceInstance(config);
        registerCenter.register(serviceDefinition, serviceInstance);
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                logger.info("refresh service and instance: {} {}", serviceDefinition.getUniqueId(),
                        JSON.toJSON(serviceInstanceSet));
                final DynamicConfigManager dynamicConfigManager = DynamicConfigManager.getInstance();
                dynamicConfigManager.addServiceInstance(serviceDefinition.getUniqueId(), serviceInstanceSet);
            }
        });
        return registerCenter;
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
