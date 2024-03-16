package org.example.gateway.client.support.springmvc;

import org.example.gateway.client.core.ApiAnnotationScanner;
import org.example.gateway.client.core.ApiProperties;
import org.example.gateway.client.support.AbstractClientRegisterManager;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.utils.NetUtils;
import org.example.gateway.common.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.example.gateway.common.constants.BasicConst.COLON_SEPARATOR;
import static org.example.gateway.common.constants.GatewayConst.DEFAULT_WEIGHT;

public class SpringMVCClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringMVCClientRegisterManager.class);

    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    // 记录处理过的bean
    private Set<Object> set = new HashSet<>();

    public SpringMVCClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    /**
     * 通过实现ApplicationContextAware接口，拿到IoC容器，用于后续使用
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 通过实现ApplicationListener接口，监听容器的事件，做相应的操作
     * @param applicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // 当Spring容器启动时，就可以注册项目信息
        if(applicationEvent instanceof ApplicationStartedEvent) {
            try {
                doRegisterSpringMVC();
            }catch (Exception e) {
                logger.error("doRegisterSpringMVC error", e);
                throw new RuntimeException(e);
            }
            logger.info("SpringMVC api started");
        }
    }


    /**
     * 向注册中心注册服务
     */
    private void doRegisterSpringMVC() {
        final Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);
        for(RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()) {
            final Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for(Map.Entry<RequestMappingInfo, HandlerMethod> me: handlerMethods.entrySet()) {
                final HandlerMethod handlerMethod = me.getValue();
                final Class<?> beanType = handlerMethod.getBeanType();
                final Object bean = applicationContext.getBean(beanType);
                // 如果此bean已经处理过就跳过不再处理
                if(set.contains(bean)) {
                    continue;
                }
                // 扫描bean中的存在的网关的注解
                final ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);
                if(serviceDefinition == null) {
                    continue;
                }
                serviceDefinition.setEnvType(getApiProperties().getEnv());
                // 封装服务实例
                final ServiceInstance serviceInstance = new ServiceInstance();
                final String localIp = NetUtils.getLocalIp();
                final Integer port = serverProperties.getPort();
                String serviceInstanceId = localIp + COLON_SEPARATOR + port;
                final String uniqueId = serviceDefinition.getUniqueId();
                final String version = serviceDefinition.getVersion();
                serviceInstance.setServiceInstanceId(serviceInstanceId);
                serviceInstance.setUniqueId(uniqueId);
                serviceInstance.setIp(localIp);
                serviceInstance.setPort(port);
                serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                serviceInstance.setVersion(version);
                serviceInstance.setWeight(DEFAULT_WEIGHT);
                // 判断是否是灰度服务
                if(getApiProperties().isGray()) {
                    serviceInstance.setGray(true);
                }
                // 注册
                register(serviceDefinition, serviceInstance);
            }
        }
    }

}
