package org.example.gateway.core.filter.router.executor;

import org.asynchttpclient.Request;
import org.example.gateway.common.config.*;
import org.example.gateway.common.utils.JSONUtil;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class DubboExecutor extends BaseExecutor{

    private static final Logger logger = LoggerFactory.getLogger(DubboExecutor.class);

    private static DubboExecutor instance;

    private DubboExecutor(Filter filter) {
        super(filter);
    }


    public static DubboExecutor getInstance(Filter filter) {
        if(instance == null) {
            synchronized (HttpExecutor.class) {
                instance = new DubboExecutor(filter);
            }
        }
        return instance;
    }


    /**
     * 调用下游的dubbo服务
     * @param ctx
     * @param hystrixConfig
     * @return
     */
    @Override
    protected Object route(GatewayContext ctx, Optional<Rule.HystrixConfig> hystrixConfig) {
        final Request request = ctx.getRequest().build();
        Object result = null;
        Throwable throwable = null;
        try {
            // 拿到dubbo服务定义
            final ServiceInstance serviceInstance = ctx.getServiceInstance();
            final ServiceDefinition serviceDefinition = ctx.getServiceDefinition();
            final Map<String, String> invokerMap = serviceDefinition.getInvokerMap();
            final DubboServiceInvoker serviceInvoker = JSONUtil.parse(invokerMap.get(ctx.getRequest().getUri()), DubboServiceInvoker.class);
            // 设置dubbo泛化调用信息
            final ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(serviceDefinition.getServiceId());
            final RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(serviceInvoker.getRegisterAddress());
            applicationConfig.setRegistry(registryConfig);
            applicationConfig.setQosEnable(false);
            final ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
            referenceConfig.setGeneric(true);
            referenceConfig.setApplication(applicationConfig);
            referenceConfig.setInterface(serviceInvoker.getInterfaceClass());
            String url = serviceDefinition.getProtocol() + "://"+serviceInstance.getIp()+":"+serviceInstance.getPort()+"/"+serviceInvoker.getInterfaceClass();
            referenceConfig.setUrl(url);
            final GenericService genericService = referenceConfig.get();
            // TODO 解析参数
            result = genericService.$invoke(serviceInvoker.getMethodName(), serviceInvoker.getParameterTypes(), new Object[]{"hehe"});
        }catch (Throwable t) {
            logger.error("execute dubbo service error", t);
            throwable = t;
        }
        complete(request, result, throwable, ctx, hystrixConfig);
        return null;
    }

}
