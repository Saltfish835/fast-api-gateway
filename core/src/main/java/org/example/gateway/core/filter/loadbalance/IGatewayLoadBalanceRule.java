package org.example.gateway.core.filter.loadbalance;

import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.core.context.GatewayContext;

public interface IGatewayLoadBalanceRule {

    /**
     * 通过上下文获取服务实例
     * @param ctx
     * @return
     */
    ServiceInstance choose(GatewayContext ctx);

    /**
     * 通过服务id获取服务实例
     * @param serviceId
     * @return
     */
    ServiceInstance choose(String serviceId, boolean gray);

}
