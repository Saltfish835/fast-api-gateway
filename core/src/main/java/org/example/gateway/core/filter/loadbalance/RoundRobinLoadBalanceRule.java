package org.example.gateway.core.filter.loadbalance;


import org.example.gateway.common.config.DynamicConfigManager;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.context.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 */
public class RoundRobinLoadBalanceRule implements IGatewayLoadBalanceRule{

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalanceRule.class);

    /**
     * 当前轮询到的位置
     */
    private AtomicInteger position = new AtomicInteger(1);

    private final String serviceId;

    private static ConcurrentHashMap<String, RoundRobinLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();


    public RoundRobinLoadBalanceRule(String serviceId) {
        this.serviceId = serviceId;
    }


    public static RoundRobinLoadBalanceRule getInstance(String serviceId) {
        RoundRobinLoadBalanceRule roundRobinLoadBalanceRule = serviceMap.get(serviceId);
        if(roundRobinLoadBalanceRule == null) {
            roundRobinLoadBalanceRule = new RoundRobinLoadBalanceRule(serviceId);
            serviceMap.put(serviceId, roundRobinLoadBalanceRule);
        }
        return roundRobinLoadBalanceRule;
    }



    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        final String serviceId = ctx.getUniqueId();
        return choose(serviceId);
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        final Set<ServiceInstance> serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId);
        if(serviceInstanceSet.isEmpty()) {
            logger.warn("No instance available for:{}",serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> instances = new ArrayList<>(serviceInstanceSet);
        if(instances.isEmpty()) {
            logger.warn("No instance available for service:{}",serviceId);
            return null;
        }else {
            final int pos = Math.abs(this.position.incrementAndGet());
            return instances.get(pos % instances.size());
        }
    }
}
