package org.example.gateway.core.filter.loadbalance;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.example.gateway.common.constants.FilterConst.*;

@FilterAspect(id=LOAD_BALANCE_FILTER_ID, name=LOAD_BALANCE_FILTER_NAME, order=LOAD_BALANCE_FILTER_ORDER)
public class LoadBalanceFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(LoadBalanceFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        final String serviceId = ctx.getUniqueId();
        // 获取负载均衡的规则
        final IGatewayLoadBalanceRule loadBalanceRule = getLoadBalanceRule(ctx);
        // 根据负载均衡的规则拿到一个服务实例
        final ServiceInstance serviceInstance = loadBalanceRule.choose(serviceId, ctx.isGray());
        final GatewayRequest request = ctx.getRequest();
        if(serviceInstance != null && request != null) {
            String host = serviceInstance.getIp() + ":" + serviceInstance.getPort();
            request.setModifyHost(host);
        }else {
            logger.warn("No instance available for : {}", serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }


    /**
     * 获取负载均衡器
     * @param ctx
     * @return
     */
    public IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        IGatewayLoadBalanceRule loadBalanceRule = null;
        final Rule rule = ctx.getRule();
        // rule中会配置负载均衡策略
        if(rule != null) {
            final Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
            for(Rule.FilterConfig filterConfig : filterConfigs) {
                if(filterConfig == null) {
                    continue;
                }
                final String filterConfigId = filterConfig.getId();
                // 如果rule有配置负载均衡策略
                if(filterConfigId.equals(LOAD_BALANCE_FILTER_ID)) {
                    final String config = filterConfig.getConfig();
                    String strategy = LOAD_BALANCE_STRATEGY_RANDOM;
                    if(StringUtils.isNotEmpty(config)) {
                        Map<String, String> mapTypeMap = JSON.parseObject(config, Map.class);
                        // 没有从rule中拿到负载均衡策略就使用默认策略
                        strategy = mapTypeMap.getOrDefault(LOAD_BALANCE_KEY, strategy);
                    }
                    switch (strategy){
                        case LOAD_BALANCE_STRATEGY_RANDOM:
                            loadBalanceRule = RandomLoadBalanceRule.getInstance(rule.getServiceId());
                            break;
                        case LOAD_BALANCE_STRATEGY_ROUND_ROBIN:
                            loadBalanceRule = RoundRobinLoadBalanceRule.getInstance(rule.getServiceId());
                            break;
                        default:
                            loadBalanceRule = RandomLoadBalanceRule.getInstance(rule.getServiceId());
                            break;
                    }
                }
            }
        }
        return loadBalanceRule;
    }
}
