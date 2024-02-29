package org.example.gateway.client.support.dubbo;

import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
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
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

import static org.example.gateway.common.constants.BasicConst.COLON_SEPARATOR;
import static org.example.gateway.common.constants.GatewayConst.DEFAULT_WEIGHT;

public class Dubbo27ClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(Dubbo27ClientRegisterManager.class);

    private Set<Object> set = new HashSet<>();;

    public Dubbo27ClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // dubbo提供的服务
        if(applicationEvent instanceof ServiceBeanExportedEvent) {
            try{
                final ServiceBean serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
                doRegisterDubbo(serviceBean);
            }catch (Exception e) {
                logger.error("doRegisterDubbo error", e);
                throw new RuntimeException(e);
            }
        }else if(applicationEvent instanceof ApplicationStartedEvent) {
            logger.info("dubbo api started");
        }
    }

    private void doRegisterDubbo(ServiceBean serviceBean) {
        final Object beanRef = serviceBean.getRef();
        if(set.contains(beanRef)) {
            return;
        }
        final ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(beanRef, serviceBean);
        if(serviceBean == null) {
            return;
        }
        serviceDefinition.setEnvType(getApiProperties().getEnv());
        // 封装服务实例
        final ServiceInstance serviceInstance = new ServiceInstance();
        final String localIp = NetUtils.getLocalIp();
        final Integer port = serviceBean.getProtocol().getPort();
        String serviceInstanceId = localIp + COLON_SEPARATOR + port;
        String uniqueId = serviceDefinition.getUniqueId();
        String version = serviceDefinition.getVersion();
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setUniqueId(uniqueId);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        serviceInstance.setVersion(version);
        serviceInstance.setWeight(DEFAULT_WEIGHT);
        register(serviceDefinition, serviceInstance);
    }

}
