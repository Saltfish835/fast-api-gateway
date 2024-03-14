package org.example.gateway.core.filter.loadbalance;

import org.example.gateway.common.config.DynamicConfigManager;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.context.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.gateway.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * 随机负载均衡
 */
public class RandomLoadBalanceRule implements IGatewayLoadBalanceRule{

    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalanceRule.class);

    /**
     * 服务id
     */
    private final String serviceId;

    /**
     * 服务列表
     */
//    private Set<ServiceInstance> serviceInstanceSet = new HashSet<>();

    private static ConcurrentHashMap<String, RandomLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();


    public RandomLoadBalanceRule(String serviceId) {
        this.serviceId = serviceId;
    }


    public static RandomLoadBalanceRule getInstance(String serviceId) {
        RandomLoadBalanceRule randomLoadBalanceRule = serviceMap.get(serviceId);
        if(randomLoadBalanceRule == null) {
            randomLoadBalanceRule = new RandomLoadBalanceRule(serviceId);
            serviceMap.put(serviceId, randomLoadBalanceRule);
        }
        return randomLoadBalanceRule;
    }


    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        final String serviceId = ctx.getUniqueId();
        return choose(serviceId, ctx.isGray());
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        final Set<ServiceInstance> serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId, gray);
        if(serviceInstanceSet.isEmpty()) {
            logger.warn("No instance available for:{}", serviceId);
            throw  new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> instanceList = new ArrayList<>(serviceInstanceSet);
        final int nextInt = ThreadLocalRandom.current().nextInt(instanceList.size());
        final ServiceInstance serviceInstance = instanceList.get(nextInt);
        return serviceInstance;
    }
}
