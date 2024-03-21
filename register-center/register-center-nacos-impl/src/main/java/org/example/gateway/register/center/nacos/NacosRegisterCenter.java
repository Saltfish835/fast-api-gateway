package org.example.gateway.register.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.constants.GatewayConst;
import org.example.gateway.common.utils.JSONUtil;
import org.example.gateway.register.center.api.RegisterCenter;
import org.example.gateway.register.center.api.RegisterCenterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NacosRegisterCenter implements RegisterCenter {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegisterCenter.class);

    private String registerAddress;

    private String env;

    private NamingService namingService;

    private NamingMaintainService namingMaintainService;

    private List<RegisterCenterListener> registerCenterListenerList = new CopyOnWriteArrayList<>();

    public NacosRegisterCenter() {
    }

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env  = env;
        try{
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try{
            final Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setIp(serviceInstance.getIp());
            nacosInstance.setPort(serviceInstance.getPort());
            Map<String, String> metadata = new HashMap<>();
            metadata.put(GatewayConst.META_DATA_KEY, JSONUtil.toJSONString(serviceInstance));
            nacosInstance.setMetadata(metadata);
            // 注册
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);
            // 更新服务定义
            Map<String, String> tmpMap = new HashMap<>();
            tmpMap.put(GatewayConst.META_DATA_KEY, JSONUtil.toJSONString(serviceDefinition));
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0, tmpMap);
            logger.info("register {} {}", serviceDefinition, serviceInstance);
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try{
            namingService.deregisterInstance(serviceDefinition.getServiceId(), env, serviceInstance.getIp(), serviceInstance.getPort());
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener registerCenterListener) {
        registerCenterListenerList.add(registerCenterListener);
        doSubscribeAllServices();
        // 可能有新服务加入，所以需要有一个定时任务来检查
        final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        // 10s执行一次
        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            doSubscribeAllServices();
        }, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * 具体的订阅操作
     */
    private void doSubscribeAllServices() {
        try {
            final Set<String> subscribeService = namingService.getSubscribeServices().stream().map(ServiceInfo::getName).collect(Collectors.toSet());
            int pageNo = 1;
            int pageSize = 100;
            final NacosRegisterListener eventListener = new NacosRegisterListener();
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();
            while(CollectionUtils.isNotEmpty(serviceList)) {
                logger.info("service list size {}", serviceList.size());
                for(String service : serviceList) {
                    if(subscribeService.contains(service)) {
                        continue;
                    }
                    namingService.subscribe(service, eventListener);
                    logger.info("subscribe {} {}", service, env);
                }
                serviceList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 监听到事件后进行回调操作
     */
    public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            if(event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                final String serviceName = namingEvent.getServiceName();
                try {
                    final Service service = namingMaintainService.queryService(serviceName, env);
                    ServiceDefinition serviceDefinition = JSON.parseObject(service.getMetadata()
                            .get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);
                    final List<Instance> allInstances = namingService.getAllInstances(serviceName, env);
                    Set<ServiceInstance> set = new HashSet<>();
                    for(Instance instance : allInstances) {
                        final ServiceInstance serviceInstance = JSON.parseObject(instance.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        set.add(serviceInstance);
                    }
                    registerCenterListenerList.stream().forEach(registerCenterListener -> {
                        registerCenterListener.onChange(serviceDefinition, set);
                    });
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
